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
The **active FE/t is the sum** of each selected scanner entry's active cost;
the passive FE/t is also additive before the integration multiplier. Range uses
the largest selected range, and the resulting scanner currently shares one color.
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

## Combining multiple selections and energy cost

Container-level `max_rolls` controls how many individual entries of the same
`ModuleDefinition` property type can appear in a single generated module:

| Container | Max entries | Selection weight |
| --- | ---: | ---: |
| `attribute_properties` | 6 | 12 |
| `conditional_attribute_properties` | 2 | 2 |
| `effects_properties` | 3 | 5 |
| `block_scanner_properties` | 4 | 4 |

These are upper limits, not guaranteed counts. The module's rolled size and
total property-count cap still apply (currently up to six entries overall).

Each selected entry contributes its own passive FE/t requirement. The passive
requirements are **summed**, then multiplied by the overall
`integration_multipliers` entry for the total property count. Multi-target
scanners also **sum** the individual selected active scan costs, with the same
integration scaling used by other active generated abilities. This means more
effect choices, attributes, or scanner targets can yield stronger modules, but
they also incur substantially higher energy demand.

## Bundled effect and scanner choices

The bundled `effects_properties.json` currently includes:

- **Exploration/survival:** Night Vision, Water Breathing, Slow Falling, Fire Resistance, Invisibility.
- **Mobility/utility:** Haste, Jump Boost, Speed.
- **Combat/sustain:** Strength, Resistance, Regeneration.

The same `effects` map accepts any valid modded MobEffect registry ID.
Choices have per-tier energy requirements and minimum area. Experimental
Haste and Jump Boost grant level II; Resistance and Regeneration remain
level I due to their strong permanent effects. Stronger benefits generally
consume more passive FE/t.

The bundled `block_scanner_properties.json` includes these target choices:

- **Ores:** `#c:ores`, `#c:ores/iron`, `#c:ores/copper`,
  `#c:ores/gold`, `#c:ores/redstone`, `#c:ores/diamond`,
  `#c:ores/emerald`, and `#c:ores/netherite_scrap`.
- **Storage:** `#c:chests`, `#c:barrels`.
- **Dungeon exploration:** `minecraft:spawner`,
  `minecraft:trial_spawner`, `minecraft:vault`.

Specialized scanners generally cost less than broad ore scanning at the
same tier. Dungeon and high-value ore scanners require larger modules and
more FE/t. Scanner colors are chosen per target; blocks from separate
scanner modules retain their respective colors. A module with several
combined scanner targets uses one color for its combined target set.

The target lists can be further extended via datapacks without adding Java
constants or switch cases. New modded block IDs need to be registered, and
block tags must exist and contain blocks to yield scanner hits.
