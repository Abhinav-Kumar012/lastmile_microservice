import requests
import datetime
import time

USERSERVICE = "http://localhost:8081"
DRIVERSERVICE = "http://localhost:8082"
RIDERSERVICE = "http://localhost:8082"
# register a user
def register_a_rider_and_driver(rider_name,rider_pass,driver_name,driver_pass):
    url = USERSERVICE + "/users/register"
    params = {
        "username" : rider_name,
        "password" : rider_pass,
        "roles" : ["rider"]
    }
    response = requests.post(url=url,json=params)
    print(response.content)
    params["username"] = driver_name
    params["password"] = driver_pass
    params["roles"] = ["driver"]
    response = requests.post(url=url,json=params)
    print(response.content)

def login(rider_name,rider_pass,driver_name,driver_pass):
    url = USERSERVICE + "/users/login"
    params = {
        "username" : rider_name,
        "password" : rider_pass,
        "role" : "rider"
    }
    response = requests.post(url=url,json=params)
    rider_jwt = response.json().get("token")
    params["username"] = driver_name
    params["password"] = driver_pass
    params["role"] = "driver"
    response = requests.post(url=url,json=params)
    driver_jwt = response.json().get("token")
    print(f"rider jwt : {rider_jwt}\ndriver jwt : {driver_jwt}")
    return (rider_jwt,driver_jwt)

def register_route(jwt_token,starting,dest,seats):
    url = DRIVERSERVICE + "/api/register-route"
    headers = {
        "Authorization": f"Bearer {jwt_token}",
        "Content-Type": "application/json"
    }
    params = {
        "startinglocation" : starting,
        "destination" : dest,
        "available_seats" : seats
    }
    data = requests.post(url=url,headers=headers,json=params).json()
    return data.get("route_id")


def register_arrival(jwt,dest,station):
    url = RIDERSERVICE + "/api/register-arrival"
    headers = {
        "Authorization": f"Bearer {jwt}",
        "Content-Type": "application/json"
    }
    time = (datetime.datetime.now() + datetime.timedelta(minutes=6)).strftime("%Y-%m-%dT%H:%M:%S")
    params = {
        "arrivaltime" : time,
        "destination" : dest,
        "arrivalstationname" : station
    }
    data = requests.post(url=url,headers=headers,json=params).json()
    print(data)

def update_loc(jwt,routeid,loc):
    url = DRIVERSERVICE + "/api/register-route"
    headers = {
        "Authorization": f"Bearer {jwt}",
        "Content-Type": "application/json"
    }
    params = {
        "route_id" : routeid,
        "location" : loc
    }
    data = requests.post(url=url,headers=headers,json=params).json()
    print(data)

def confirm_ride(jwt,)

dest = "marathalli"

register_a_rider_and_driver("abhi","abhi","sid","sid")
tokens = login("abhi","abhi","sid","sid")
route_id = register_route(tokens[1],"electronic_city",dest,4)
register_arrival(tokens[0],dest,"METROSTATION_SILKBOARD")
update_loc(tokens[1],route_id,"hsrlayout")
time.sleep(60*1)
