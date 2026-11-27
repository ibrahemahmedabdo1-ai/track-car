# Android Device Compatibility Matrix

Track Car supports a capability-based approach rather than a single universal firmware package.

## Data collected during diagnostics
- Manufacturer and brand
- Model and device code
- Android release and SDK level
- Build fingerprint and security patch level
- CPU ABI list
- Available location providers and required permissions
- Bootloader/root status only when the device owner explicitly requests a diagnostic

## Capability states
- **Standard Android:** install the signed Agent through the normal Android package installer.
- **Managed / pre-authorized device:** use an approved management or pre-installed bootstrap component.
- **Root-capable:** enable only the root operations that pass a device-specific verification and user authorization.
- **Unsupported / unknown:** do not flash or install a guessed package; report the diagnostic profile for review.

## Root package policy
There is no safe universal root ZIP or boot image for all Android head units. A root method must match the exact model, firmware/build fingerprint, boot image and bootloader state. A mismatched image can brick the device or erase data.

## Release gates
Before adding a model profile:
1. Test on a physical device of that exact model and firmware.
2. Verify install, update, rollback and uninstall paths.
3. Verify location behavior after reboot and under battery restrictions.
4. Verify signature and checksum validation.
5. Document recovery steps and known limitations.
