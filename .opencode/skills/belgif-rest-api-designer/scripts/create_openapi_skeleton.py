#!/usr/bin/env python3
"""Create a Belgif-oriented OpenAPI skeleton.
Usage: python scripts/create_openapi_skeleton.py --title "My API" --resource declarations --base-url https://api.example.gov.be/my/v1
"""
import argparse, re, sys
from pathlib import Path

TEMPLATE = Path(__file__).resolve().parents[1] / 'assets' / 'openapi-template.yaml'

def main():
    p = argparse.ArgumentParser()
    p.add_argument('--title', required=True)
    p.add_argument('--resource', required=True, help='Plural resource name, e.g. declarations')
    p.add_argument('--base-url', required=True)
    p.add_argument('--out', default='openapi.yaml')
    args = p.parse_args()
    if not re.fullmatch(r'[a-z][a-z0-9-]*', args.resource):
        sys.exit('Resource must be lowercase kebab-case/plural-like, e.g. declarations or tax-debts')
    data = TEMPLATE.read_text(encoding='utf-8')
    singular = args.resource[:-1] if args.resource.endswith('s') else args.resource
    pascal = ''.join(part.capitalize() for part in singular.split('-'))
    data = data.replace('Example Belgif REST API', args.title)
    data = data.replace('https://api.example.gov.be/example/v1', args.base_url.rstrip('/'))
    data = data.replace('declarations', args.resource)
    data = data.replace('Declarations', ''.join(part.capitalize() for part in args.resource.split('-')))
    data = data.replace('Declaration', pascal)
    data = data.replace('declaration', singular)
    Path(args.out).write_text(data, encoding='utf-8')
    print(f'Wrote {args.out}')

if __name__ == '__main__':
    main()
