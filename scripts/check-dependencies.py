import xml.etree.ElementTree as ET
import sys
import os
from packaging.version import Version

REQUIRED_VERSIONS = {
    "com.fasterxml.jackson.core:jackson-databind": "2.17.0",
    "com.fasterxml.jackson.datatype:jackson-datatype-jsr310": "2.17.0",
    "com.mysql:mysql-connector-j": "8.3.0",
}


def parse_pom(pom_path):
    tree = ET.parse(pom_path)
    root = tree.getroot()
    ns_uri = "http://maven.apache.org/POM/4.0.0"

    properties = {}
    for prop in root.findall(f"{{{ns_uri}}}properties/*"):
        tag = prop.tag.replace(f"{{{ns_uri}}}", "")
        properties[tag] = prop.text.strip() if prop.text else ""

    dependencies = []
    for dep in root.findall(f".//{{{ns_uri}}}dependency"):
        group_id_el = dep.find(f"{{{ns_uri}}}groupId")
        artifact_id_el = dep.find(f"{{{ns_uri}}}artifactId")
        version_el = dep.find(f"{{{ns_uri}}}version")

        group_id = group_id_el.text.strip() if group_id_el is not None and group_id_el.text else ""
        artifact_id = artifact_id_el.text.strip() if artifact_id_el is not None and artifact_id_el.text else ""
        raw_version = version_el.text.strip() if version_el is not None and version_el.text else ""

        if raw_version.startswith("${") and raw_version.endswith("}"):
            prop_key = raw_version[2:-1]
            resolved_version = properties.get(prop_key, raw_version)
        else:
            resolved_version = raw_version

        dependencies.append({
            "groupId": group_id,
            "artifactId": artifact_id,
            "version": resolved_version,
            "rawVersion": raw_version,
        })

    return dependencies


def check_dependencies(pom_path):
    if not os.path.isfile(pom_path):
        print(f"ERROR: pom.xml not found at {pom_path}")
        sys.exit(1)

    dependencies = parse_pom(pom_path)
    dep_map = {f"{d['groupId']}:{d['artifactId']}": d for d in dependencies}

    errors = []
    passed = []

    for coord, min_version_str in REQUIRED_VERSIONS.items():
        min_version = Version(min_version_str)

        if coord not in dep_map:
            errors.append(
                f"MISSING: Dependency '{coord}' is not declared in pom.xml. "
                f"Required version >= {min_version_str}"
            )
            continue

        dep = dep_map[coord]
        actual_version_str = dep["version"]

        try:
            actual_version = Version(actual_version_str)
        except Exception:
            errors.append(
                f"PARSE ERROR: Cannot parse version '{actual_version_str}' for '{coord}'"
            )
            continue

        if actual_version >= min_version:
            passed.append(
                f"OK: {coord} version {actual_version_str} >= {min_version_str}"
            )
        else:
            errors.append(
                f"VULNERABLE: {coord} version {actual_version_str} is below "
                f"minimum required version {min_version_str}. "
                f"Please upgrade to fix known security vulnerabilities."
            )

    print("=" * 60)
    print("Dependency Security Check Report")
    print("=" * 60)

    if passed:
        print("\n[PASSED]")
        for msg in passed:
            print(f"  + {msg}")

    if errors:
        print("\n[FAILED]")
        for msg in errors:
            print(f"  x {msg}")
        print("\n" + "=" * 60)
        print(f"Check FAILED: {len(errors)} issue(s) found.")
        print("=" * 60)
        sys.exit(1)
    else:
        print("\n" + "=" * 60)
        print("Check PASSED: All dependencies meet security requirements.")
        print("=" * 60)


if __name__ == "__main__":
    pom_path = sys.argv[1] if len(sys.argv) > 1 else "backend/pom.xml"
    check_dependencies(pom_path)
