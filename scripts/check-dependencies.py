#!/usr/bin/env python3
import xml.etree.ElementTree as ET
import sys
import os


def parse_version(version_str):
    parts = version_str.split('.')
    return tuple(int(p) if p.isdigit() else 0 for p in parts[:3])


def check_dependencies(pom_path):
    if not os.path.exists(pom_path):
        print(f"ERROR: pom.xml not found at {pom_path}")
        return False

    tree = ET.parse(pom_path)
    root = tree.getroot()

    ns = {'m': 'http://maven.apache.org/POM/4.0.0'}

    required = {
        'jackson-databind': {'groupId': 'com.fasterxml.jackson.core', 'min_version': '2.17.0'},
        'jackson-datatype-jsr310': {'groupId': 'com.fasterxml.jackson.datatype', 'min_version': '2.17.0'},
        'mysql-connector-j': {'groupId': 'com.mysql', 'min_version': '8.3.0'}
    }

    found_versions = {}
    errors = []

    for dep in root.findall('.//m:dependency', ns):
        group_id = dep.find('m:groupId', ns)
        artifact_id = dep.find('m:artifactId', ns)
        version = dep.find('m:version', ns)

        if group_id is None or artifact_id is None or version is None:
            continue

        gid = group_id.text
        aid = artifact_id.text
        ver = version.text

        for name, req in required.items():
            if gid == req['groupId'] and aid == name:
                found_versions[name] = ver

    for name, req in required.items():
        if name not in found_versions:
            errors.append(f"Dependency not found: {req['groupId']}:{name}")
            continue

        current = found_versions[name]
        min_ver = req['min_version']

        current_tuple = parse_version(current)
        min_tuple = parse_version(min_ver)

        if current_tuple < min_tuple:
            errors.append(
                f"[SECURITY] {req['groupId']}:{name} version {current} is below minimum required {min_ver}"
            )
        else:
            print(f"[OK] {req['groupId']}:{name} version {current} >= {min_ver}")

    if errors:
        print("\n" + "=" * 60)
        print("DEPENDENCY SECURITY CHECK FAILED")
        print("=" * 60)
        for err in errors:
            print(f"  - {err}")
        print("=" * 60 + "\n")
        return False

    print("\nAll dependency security checks passed.")
    return True


if __name__ == "__main__":
    pom_path = os.path.join(os.path.dirname(__file__), '..', 'backend', 'pom.xml')
    pom_path = os.path.abspath(pom_path)
    success = check_dependencies(pom_path)
    sys.exit(0 if success else 1)
