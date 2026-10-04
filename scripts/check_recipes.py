#!/usr/bin/env python3
"""Check recipe references against the item registrations in this source tree."""

import json
import re
import sys
from collections import defaultdict
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
REGISTRY = ROOT / "src/main/java/com/github/littleemptydoll/exoequipment/registry"
RECIPES = ROOT / "src/main/resources/data/exoequipment/recipe"
NAMESPACE = "exoequipment:"


def source(name):
    return (REGISTRY / f"{name}.java").read_text(encoding="utf-8")


def registered_items():
    module_classes = re.findall(r"(Mod\w+Modules)\.register\(REGISTRY\)", source("ModModules"))
    assert module_classes, "No module registration classes found"
    modules = []
    for class_name in module_classes:
        modules.extend(re.findall(
            r'\b(?:registry\.register|register\w*)\s*\(\s*(?:registry\s*,\s*)?"([a-z0-9_]+)"',
            source(class_name),
        ))
    assert modules, "No module registrations found"
    module_ids = {NAMESPACE + name + "_module" for name in modules}
    assert len(module_ids) == len(modules), "Duplicate module registration"

    items = set(module_ids)
    items.update(NAMESPACE + name for name in re.findall(
        r'(?:ITEMS\.register|part)\("([a-z0-9_]+)"', source("ModItems")
    ))
    for class_name, suffix in (
        ("ModFrames", "_frame"),
        ("ModMatrices", "_matrix"),
        ("ModControllers", "_controller"),
        ("ModEnergySystems", "_energy_system"),
        ("ModExoskeletons", "_exoskeleton"),
    ):
        names = re.findall(r'REGISTRY\.register\(\s*"([a-z0-9_]+)"', source(class_name))
        assert names, f"No registrations found in {class_name}"
        items.update(NAMESPACE + name + suffix for name in names)
    return module_ids, items


def item_references(node):
    if isinstance(node, dict):
        for key, value in node.items():
            if key in ("item", "items") and isinstance(value, str):
                yield value
            yield from item_references(value)
    elif isinstance(node, list):
        for value in node:
            yield from item_references(value)


def main():
    modules, items = registered_items()
    outputs = defaultdict(list)
    signatures = defaultdict(list)
    errors = []
    paths = sorted(RECIPES.glob("*.json"))
    assert paths, "No recipes found"

    for path in paths:
        try:
            recipe = json.loads(path.read_text(encoding="utf-8"))
        except (ValueError, OSError) as exc:
            errors.append(f"{path.name}: invalid JSON: {exc}")
            continue
        result = recipe.get("result", {})
        item = result.get("id") if isinstance(result, dict) else None
        if not isinstance(item, str):
            errors.append(f"{path.name}: missing result ID")
            continue
        outputs[item].append(path.name)
        if item.startswith(NAMESPACE) and item not in items:
            errors.append(f"{path.name}: unregistered result {item}")
        for reference in item_references({key: value for key, value in recipe.items() if key != "result"}):
            if reference.startswith(NAMESPACE) and reference not in items:
                errors.append(f"{path.name}: unregistered ingredient or condition {reference}")

        if recipe.get("type") == "exoequipment:fabricator":
            if not recipe.get("ingredients") or not isinstance(recipe.get("energy"), int) or recipe["energy"] <= 0:
                errors.append(f"{path.name}: missing ingredients or positive energy")
            for ingredient in recipe.get("ingredients", []):
                if (not isinstance(ingredient, dict) or not isinstance(ingredient.get("count"), int)
                        or ingredient["count"] <= 0 or not isinstance(ingredient.get("ingredient"), dict)):
                    errors.append(f"{path.name}: invalid counted ingredient")

        # Alternative material recipes may share a result; identical inputs and conditions may not.
        signature = json.dumps((item, recipe.get("ingredients"), recipe.get("pattern"),
                                recipe.get("key"), recipe.get("ingredient"),
                                recipe.get("neoforge:conditions")), sort_keys=True)
        signatures[signature].append(path.name)

    for names in signatures.values():
        if len(names) > 1:
            errors.append(f"Duplicate recipe: {', '.join(names)}")
    for item in sorted(modules - set(outputs) - {NAMESPACE + "creative_generator_module"}):
        errors.append(f"Missing recipe for registered module {item}")

    if errors:
        print("Recipe audit failed:\n" + "\n".join(errors), file=sys.stderr)
        return 1
    print(f"Checked {len(paths)} recipes and {len(modules)} registered modules; no missing or invalid references")
    return 0


if __name__ == "__main__":
    sys.exit(main())
