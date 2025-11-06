# Sage Plugin Repository Overview

## Project Summary
This repository contains the source code for the Sage Minecraft server plugin. The plugin offers a collection of quality-of-life utilities, staff moderation tools, and teleportation features built on top of the Spigot/Bukkit API.

## Build & Run Instructions
1. Install JDK 17 or newer.
2. Run `gradlew build` to produce the plugin JAR in `build/libs`.
3. Copy the generated JAR into your server's `plugins` directory and restart the server.

## Coding Conventions
- Use standard Java naming conventions for packages, classes, and members.
- Prefer descriptive variable names and clear control flow.
- Avoid `//` comments; use block comments `/* ... */` where documentation is required.
- Keep command messages color-coded with Minecraft formatting codes.
- Ensure all player interactions respect permission checks and null-safety.