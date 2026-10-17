#!/usr/bin/env python3
"""Static validation for generated-module property containers.

Runtime registry lookups remain authoritative for optional modded attributes,
effects and blocks. This check protects the JSON descriptor schema itself.
"""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/resources/data/exoequipment/generated_module_properties"
RESOURCE = re.compile(r"^[a-z0-9_.-]+:[a-z0-9_./-]+$")
SECTIONS = {
    "attributes": "attribute",
    "conditional_attributes": "conditional_attribute",
    "effects": "effect",
    "damage_reductions": "damage_reduction",
    "status_protections": "status_protection",
    "scanner_targets": "block_scanner",
}
REQUIRED = {
    "attribute": ("amount",),
    "conditional_attribute": ("amount",),
    "damage_reduction": ("amount",),
    "status_protection": ("amount",),
    "block_scanner": ("range", "active_energy"),
    "storage": ("capacity", "input", "output"),
    "generation": ("amount",),
    "temperature_impact": ("resistance",),
    "shield": ("capacity", "recharge_rate", "recharge_delay"),
    "emergency_shield": ("restore", "cooldown"),
    "shield_protection": ("transfer",),
    "hunger": ("reduction",),
    "thirst": ("reduction",),
    "regeneration": ("health_per_second",),
    "revival": ("restore_health", "cooldown", "invulnerability_ticks"),
    "jetpack": ("vertical_thrust", "horizontal_speed", "active_energy"),
    "blink": ("distance", "activation_energy", "cooldown"),
    "flight": ("active_energy",),
    "thermal_vision": ("active_energy",),
    "pickup_magnet": ("radius",),
    "cloaking": ("activation_energy", "active_energy", "cooldown"),
    "laser_defense": ("range", "damage", "cooldown", "energy_cost"),
    "discharge_defense": ("range", "jump_range", "damage", "falloff",
                           "targets", "bounces", "cooldown", "energy_cost"),
    "entity_sensor": ("range", "active_energy"),
    "thermal": ("amount",),
    "temperature_modifier": (),
    "body_protection": ("chance", "reduction"),
    "body_regeneration": ("health_per_second",),
    "effect": (),
}
ERRORS = []
CHOICES = 0


def error(where, message):
    ERRORS.append(f"{where}: {message}")


def check_component(where, component, tiers):
    typ = component.get("type")
    if typ not in REQUIRED:
        error(where, f"unknown component type {typ!r}")
        return
    fields = REQUIRED[typ]
    if typ in ("attribute", "conditional_attribute") and component.get("operation") == "boolean":
        fields = ()
    for field in fields:
        if field not in component:
            error(where, f"component {typ!r} missing {field!r}")
            continue
        spec = component[field]
        if isinstance(spec, str):
            for tier, values in tiers.items():
                if spec not in values:
                    error(where, f"{tier}: unknown numeric field reference {spec!r}")
        elif isinstance(spec, dict):
            if "min" not in spec or "max" not in spec:
                error(where, f"{field}: range needs min and max")
        elif not isinstance(spec, (int, float)):
            error(where, f"{field}: expected number, tier reference or range")


def check_choice(where, choice, auto_component=None):
    global CHOICES
    CHOICES += 1
    tiers = choice.get("tiers")
    if not isinstance(tiers, dict) or not tiers:
        error(where, "choice requires nonempty tiers")
        return
    if not set(tiers).issubset({"engineering", "military", "experimental"}):
        error(where, "unknown tier key")
    if choice.get("min_area", 1) not in range(1, 17):
        error(where, "min_area must be 1..16")
    if choice.get("weight", 1) <= 0:
        error(where, "weight must be positive")
    components = ([auto_component] if auto_component else []) + choice.get("components", [])
    if not components:
        error(where, "legacy entry without components")
    for index, comp in enumerate(components):
        check_component(f"{where}.components[{index}]", comp, tiers)


def direct_component(section, key, choice):
    kind = SECTIONS[section]
    tag = key.startswith("#")
    special = key in ("default", "harmful")
    component = {"type": kind}
    if not special:
        component["id"] = choice.get("id", key.lstrip("#"))
    if kind in ("attribute", "conditional_attribute"):
        component["operation"] = choice.get("operation", "add_value")
        if component["operation"] != "boolean":
            component["amount"] = choice.get("amount", "amount")
    elif kind == "effect":
        component["level"] = choice.get("level", 1)
    elif kind in ("damage_reduction", "status_protection"):
        component["amount"] = choice.get("amount",
            "reduction" if kind == "damage_reduction" else "protection")
        component["target_type"] = choice.get("target_type",
            key if special else "tag" if tag else
            "damage_type" if kind == "damage_reduction" else "effect")
    elif kind == "block_scanner":
        component["range"] = choice.get("range", "range")
        component["active_energy"] = choice.get("active_energy", "active_energy")
        component["target_type"] = choice.get("target_type", "tag" if tag else "block")
    return component


def main():
    for path in sorted(SOURCE.glob("*.json")):
        with path.open(encoding="utf-8") as stream:
            data = json.load(stream)
        where = path.name
        if not RESOURCE.fullmatch(data.get("container", "")):
            error(where, "invalid container id")
        count = 0
        for key, choice in data.get("entries", {}).items():
            check_choice(f"{where}.entries.{key}", choice)
            count += 1
        for section in SECTIONS:
            for key, variants in data.get(section, {}).items():
                if key not in ("default", "harmful"):
                    rid = key.lstrip("#")
                    if not RESOURCE.fullmatch(rid):
                        error(where, f"invalid resource id: {key!r}")
                for index, choice in enumerate(variants if isinstance(variants, list) else [variants]):
                    name = f"{where}.{section}.{key}[{index}]"
                    check_choice(name, choice, direct_component(section, key, choice))
                    count += 1
        if data.get("selectable", True) and not count:
            error(where, "selectable container has no choices")
        if data.get("selectable", True) and data.get("max_rolls", 1) <= 0:
            error(where, "selectable container must have positive max_rolls")

    for problem in ERRORS:
        print("ERROR:", problem, file=sys.stderr)
    print(f"Validated {CHOICES} generated-module choices in "
          f"{len(list(SOURCE.glob('*.json')))} containers.")
    return 1 if ERRORS else 0


if __name__ == "__main__":
    raise SystemExit(main())
