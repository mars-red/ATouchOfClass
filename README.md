
Key rings
=========

Craft a Key Ring with four iron nuggets in a diamond pattern (top, left, right,
and bottom of a crafting grid). Hold it and right-click in the air to open its
16 slots beside your inventory. Click, drag, or shift-click to transfer keys.
Only door keys with a non-empty code set on a key cutter are accepted; each slot
holds one key. The open ring stays locked in its inventory slot until you close
the menu, and its contents travel with it and are saved with the item.
The inventory and held-item texture shows a bare silver ring when empty and
gold keys on the ring whenever it contains keys. This updates automatically
when adding the first key or removing the last one.
Key rings can be renamed in an anvil, whether empty or filled. Place the ring in
the left input slot, enter a name, and take the output (a fresh rename costs one
experience level). Renaming preserves every key and its code; the custom name
also appears as the ring's container title. Clear the anvil name field to restore
the default name.

Use a ring on any part of a tall door to open or close it. Any matching key on
the ring grants access; an empty ring or one without a matching code cannot
operate a locked door. Individual keys continue to work as before.

Installation information
=======

This template repository can be directly cloned to get you started with a new
mod. Simply create a new repository cloned from this one, by following the
instructions provided by [GitHub](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-repository-from-a-template).

Once you have your clone, simply open the repository in the IDE of your choice. The usual recommendation for an IDE is either IntelliJ IDEA or Eclipse.

If at any point you are missing libraries in your IDE, or you've run into problems you can
run `gradlew --refresh-dependencies` to refresh the local cache. `gradlew clean` to reset everything 
{this does not affect your code} and then start the process again.

Mapping Names:
============
By default, the MDK is configured to use the official mapping names from Mojang for methods and fields 
in the Minecraft codebase. These names are covered by a specific license. All modders should be aware of this
license. For the latest license text, refer to the mapping file itself, or the reference copy here:
https://github.com/NeoForged/NeoForm/blob/main/Mojang.md

Additional Resources: 
==========
Community Documentation: https://docs.neoforged.net/  
NeoForged Discord: https://discord.neoforged.net/
