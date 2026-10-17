# Generated prototype property containers

The generator picks a **ModuleDefinition property container** first, then one
available entry inside it. This is controlled by the resource JSON files at
`data/<namespace>/generated_module_properties/<path>.json`.

Each selectable container may define `weight` (selection weight) and
`max_rolls` (maximum selections from that container in one prototype).
Each choice may declare `min_area`, `weight`, `requires`, `conflicts`,
`ignore_quality`, and per-tier `tiers` values. Ranges support
`min`, `max`, `step` and `distribution`.

## Direct registry IDs, not hard-coded module names

The following **top-level maps** select targets by their exact registry IDs:

| Property container selector | What the keys mean |
| --- | --- |
| `attributes` | Minecraft/NeoForge attribute registry ID |
| `conditional_attributes` | Attribute ID with conditions |
| `effects` | Mob-effect registry ID |
| `damage_reductions` | Damage type ID, `#<tag-id>`, or `default` |
| `status_protections` | Mob-effect ID, `#<tag-id>`, or `harmful` |
| `scanner_targets` | Block ID or `#<block-tag-id>` |

For example, this is a full, valid attribute container. Its attribute key is
a real registry identifier. Changing it to an identifier registered by another
mod does **not** require a new Java constant or a builder `case`.

```json
{
  "container": "exoequipment:attribute_properties",
  "weight": 12,
  "max_rolls": 6,
  "attributes": {
    "minecraft:generic.armor": {
      "min_area": 2,
      "operation": "add_value",
      "tiers": {
        "military": {
          "energy": {"min": 26, "max": 45},
          "priority": 8,
          "amount": {"min": 4.5, "max": 7.5, "step": 0.25}
        }
      }
    }
  }
}
```

An attribute's `operation` can be `add_value`,
`add_multiplied_base`, `add_multiplied_total`, or `boolean` (a
registry attribute used as an on/off ability). Optional `multiplier`
and `scale_quality` adjust its magnitude. `conditional_attributes`
also accept `conditions` or `condition_pool`; a missing condition uses
the built-in low-health default.

An effect is similarly direct:

```json
{
  "container": "exoequipment:effects_properties",
  "max_rolls": 2,
  "effects": {
    "minecraft:night_vision": {
      "level": 1,
      "tiers": {"experimental": {"energy": 20, "priority": 4}}
    }
  }
}
```

`level` is the user-visible effect level (1 means amplifier 0). Nonexistent
optional mod attributes/effects are omitted when the data loads; the relevant
mod can additionally be declared with `requires`.

For damage/status protection, a leading `#` refers to a *tag* (not a damage
type or effect with a literal `#` in its ID):

```json
{
  "container": "exoequipment:damage_reduction_properties",
  "damage_reductions": {
    "default": {
      "tiers": {"experimental": {"reduction": 0.20, "energy": 110}}
    },
    "#minecraft:is_projectile": {
      "tiers": {"military": {"reduction": 0.45, "energy": 60}}
    }
  }
}
```

`status_protections` follows the same convention, with `harmful` as the
global fallback for harmful effects.

For block scanning:

```json
{
  "container": "exoequipment:block_scanner_properties",
  "scanner_targets": {
    "#c:ores": {
      "tiers": {
        "engineering": {
          "range": 18,
          "active_energy": 50,
          "color": 1763071,
          "energy": 20
        }
      }
    },
    "minecraft:ancient_debris": {
      "tiers": {
        "experimental": {
          "range": 28,
          "active_energy": 70,
          "color": 12679935,
          "energy": 35
        }
      }
    }
  }
}
```

If more than one scanner entry is generated, the target lists are combined.
If multiple variants for **the same target** are desired, use an array of
descriptors under that target key instead of inventing fake registry IDs.

## Other ModuleDefinition fields

Non-registry components such as `shield`, `jetpack`, `revival`,
`storage` and `thermal` use the `entries` collection, with a
`components` array that names a **component type**, not a hardcoded
alias-to-attribute mapping:

```json
{
  "container": "exoequipment:shield_properties",
  "entries": {
    "shield": {
      "min_area": 4,
      "tiers": {
        "military": {
          "capacity": {"min": 200, "max": 300},
          "recharge_rate": 2,
          "recharge_delay": 80
        }
      },
      "components": [{
        "type": "shield",
        "capacity": "capacity",
        "recharge_rate": "recharge_rate",
        "recharge_delay": "recharge_delay"
      }]
    }
  }
}
```

Strings in numeric component fields are **references to tier values**, so one
component descriptor works across all supported tiers. A choice may contribute
more than one component when an ability has multiple effects. Only the
supported `ModuleDefinition` property *shapes* need Java handlers; the
individual attribute, effect, damage type and block identifiers do not.

Use `python3 scripts/check_generated_module_properties.py` to validate
basic structure and tier references before building.
