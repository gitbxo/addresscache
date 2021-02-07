
# python3 src/simulator/simulator.py addresses.json

import html
import json
import requests
import sys


# get address
# @GetMapping(value = "/address")
# @RequestParam(name = "addressId", required = true)
def get_address(data):
  return requests.get(f"http://localhost:9000/address?addressId={html.escape(data.get('addressId') or '')}")


# search
# @GetMapping(value = "/search")
# @RequestParam(name = "query", required = true)
# @RequestParam(name = "maxResults", required = false)
# @RequestParam(name = "exactMatch", required = false)
# @RequestParam(name = "requireAll", required = false)
def search(data):
  return requests.get(f"http://localhost:9000/search?query={html.escape(data.get('query') or '')}&maxResults={html.escape(data.get('maxResults') or '10')}&exactMatch={html.escape(data.get('exactMatch') or 'false')}&requireAll={html.escape(data.get('requireAll') or 'false')}")


# update address
# @PutMapping(value = "/address")
# @RequestParam(name = "addressId", required = true)
# @RequestParam(name = "line1", required = false)
# @RequestParam(name = "line2", required = false)
# @RequestParam(name = "city", required = false)
# @RequestParam(name = "state", required = false)
# @RequestParam(name = "zip", required = false)
def update_address(data):
  return requests.put(f"http://localhost:9000/address?addressId={html.escape(data.get('addressId') or '')}&line1={html.escape(data.get('line1') or '')}&line2={html.escape(data.get('line2') or '')}&city={html.escape(data.get('city') or '')}&state={html.escape(data.get('state') or '')}&zip={html.escape(data.get('zip') or '')}")


# delete address
# @DeleteMapping(value = "/address")
# @RequestParam(name = "addressId", required = true)
def delete_address(data):
  return requests.delete(f"http://localhost:9000/address?addressId={html.escape(data.get('addressId') or '')}")


# create address
# @PostMapping(value = "/address")
# @RequestParam(name = "line1", required = true)
# @RequestParam(name = "line2", required = false)
# @RequestParam(name = "city", required = true)
# @RequestParam(name = "state", required = true)
# @RequestParam(name = "zip", required = true)
def create_address(data):
  return requests.post(f"http://localhost:9000/address?line1={html.escape(data.get('line1') or '')}&line2={html.escape(data.get('line2') or '')}&city={html.escape(data.get('city') or '')}&state={html.escape(data.get('state') or '')}&zip={html.escape(data.get('zip') or '')}")


def simulator(filename):
  address_list = []

  with open(filename) as f:
    for data in json.loads(f.read()):
      addressId = data.get('addressId') or ''
      if addressId.startswith('ref '):
        try:
          index = int(addressId.split(' ')[1])
          cached = address_list[index]
          data['addressId'] = cached
        except:
          pass

      task = data.get('task', 'create')
      result = None
      if task == 'get':
        result = get_address(data)
      elif task == 'search':
        result = search(data)
      elif task == 'update':
        result = update_address(data)
      elif task == 'delete':
        result = delete_address(data)
      else:
        result = create_address(data)
        if result.status_code == 200:
          raw_split = result._content.decode().split(':')
          address_list.append(raw_split[0])


      print('\n *** ')
      print(str(data))
      print(result)
      print(result._content.decode())
      print(' *** \n')


if __name__ == '__main__':
  if len(sys.argv) > 1 and sys.argv[1]:
    simulator(sys.argv[1])
  else:
    print('pass addresses.json filename as parameter')
    print(f'e.g. python3 {sys.argv[0]} addresses.json')

