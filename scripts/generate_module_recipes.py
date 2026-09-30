"""Fill gaps in the Fabricator's module recipes without replacing hand-tuned recipes.

Run from the repository root: python3 scripts/generate_module_recipes.py
The resulting JSON is checked into src/main/resources, so recipes also work before runData.
"""

import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
REGISTRY = ROOT / "src/main/java/com/github/littleemptydoll/exoequipment/registry"
RECIPES = ROOT / "src/main/resources/data/exoequipment/recipe"
TIERS = ("civilian", "engineering", "military", "experimental")

# Each module category uses its corresponding Fabricator parts. These are starting
# recipes; individual JSON files can subsequently be tuned without touching this script.
PARTS = {
    "energy": ("energy_component", "electronic_component"),
    "defense": ("shield_emitter", "structural_component"),
    "mobility": ("thruster", "mechanical_component"),
    "survival": ("energy_component", "electronic_component"),
    "sensor": ("sensor_array", "electronic_component"),
    "thermal": ("thermal_regulator", "structural_component"),
    "combat": ("mechanical_component", "electronic_component"),
    "utility": ("electronic_component", "mechanical_component"),
}

# Standalone experimental modules still draw on both medium-tier branches.
BRANCH_FOUNDATIONS = {
    "defense": "protection",
    "mobility": "jetpack",
    "utility": "pickup_magnet",
}

SOURCES = {
    "ModEnergyModules": "energy",
    "ModDefenseModules": "defense",
    "ModStatusProtectionModules": "defense",
    "ModBodyProtectionModules": "defense",
    "ModMobilityModules": "mobility",
    "ModSurvivalModules": "survival",
    "ModSensorModules": "sensor",
    "ModThermalModules": "thermal",
    "ModCombatModules": "combat",
    "ModUtilityModules": "utility",
}


def register(catalog, name, category, integration=None):
    if name == "creative_generator":
        return
    if name in catalog and catalog[name] != (category, integration):
        raise ValueError(f"Conflicting module registration: {name}")
    catalog[name] = (category, integration)


def catalog_from_sources():
    catalog = {}
    for filename, category in SOURCES.items():
        source = (REGISTRY / f"{filename}.java").read_text()
        for name in re.findall(r'"((?:civilian|engineering|military|experimental|night|creative)_[a-z_]+)"', source):
            register(catalog, name, category)
        # Three registries build their tiered names by appending a suffix.
        for helper in ("registerProtectionSeries", "registerSpecializedSeries"):
            for suffix in re.findall(rf'{helper}\(\s*registry,\s*"([a-z_]+)"', source):
                for tier in TIERS:
                    register(catalog, f"{tier}_{suffix}", category)

    source = (REGISTRY / "ModLsoModules.java").read_text()
    for name in re.findall(r'"((?:civilian|engineering|military|experimental)_[a-z_]+)"', source):
        category = "survival" if "thirst_assist" in name or "body_regeneration" in name else "thermal"
        register(catalog, name, category, "legendarysurvivaloverhaul")

    source = (REGISTRY / "ModAttributeModules.java").read_text()
    for series, category in re.findall(r'series\("([a-z_]+)",\s*ModuleCategory\.([A-Z]+)', source):
        integration = "apothic_attributes" if series in {
            "crit_chance", "crit_damage", "life_steal", "armor_pierce",
            "dodge_chance", "experience_gained",
        } else None
        for tier in TIERS:
            register(catalog, f"{tier}_{series}", category.lower(), integration)
    for tier in TIERS:
        register(catalog, f"{tier}_adrenaline", "combat")
    return catalog


def recipe_for(name, category, integration, catalog):
    tier, suffix = name.split("_", 1) if not name.startswith("night_") else ("civilian", name)
    primary, secondary = PARTS[category]
    ingredient_counts = []

    def add(item, count=1):
        for index, (existing, amount) in enumerate(ingredient_counts):
            if existing == item:
                ingredient_counts[index] = (item, amount + count)
                return
        ingredient_counts.append((item, count))

    def has(other_tier):
        candidate = f"{other_tier}_{suffix}"
        return candidate if candidate in catalog else None

    if tier == "civilian":
        add(primary)
        add(secondary)
        energy = 650
    elif tier in ("engineering", "military"):
        base = has("civilian")
        if base:
            add(f"{base}_module")
        else:
            add(secondary, 2)
        add(primary, 2)
        add("composite")
        add("thermal_regulator" if tier == "engineering" else "shield_emitter")
        energy = 1400 if base else 1550
    else:
        engineering, military = has("engineering"), has("military")
        for branch, predecessor in (("engineering", engineering), ("military", military)):
            if predecessor:
                add(f"{predecessor}_module")
            else:
                foundation = BRANCH_FOUNDATIONS.get(category)
                if not foundation:
                    raise ValueError(f"Missing {branch} foundation for {name}")
                add(f"{branch}_{foundation}_module")
        add("experimental_component")
        add(primary, 2)
        energy = 3700

    result = f"exoequipment:{name}_module"
    recipe = {
        "type": "exoequipment:fabricator",
        "ingredients": [
            {"ingredient": {"item": f"exoequipment:{item}"}, "count": count}
            for item, count in ingredient_counts
        ],
        "result": {"id": result, "count": 1},
        "energy": energy,
    }
    if integration:
        recipe["neoforge:conditions"] = [
            {"type": "neoforge:mod_loaded", "modid": integration},
            {"type": "neoforge:item_exists", "item": result},
        ]
    return recipe


def main():
    catalog = catalog_from_sources()
    created = []
    for name, (category, integration) in sorted(catalog.items()):
        path = RECIPES / f"{name}_module.json"
        if not path.exists():
            recipe = recipe_for(name, category, integration, catalog)
            path.write_text(json.dumps(recipe, indent=2, ensure_ascii=False) + "\n")
            created.append(name)
    print(f"Registered modules: {len(catalog)}; added recipes: {len(created)}")


if __name__ == "__main__":
    main()
