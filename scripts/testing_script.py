import requests
import datetime
import time

USERSERVICE = "http://localhost:8081"
DRIVERSERVICE = "http://localhost:8082"
RIDERSERVICE = "http://localhost:8083"
TRIPSERVICE = "http://localhost:8084"
# register a user
def register_a_rider_and_driver(rider_name,rider_pass,driver_name,driver_pass):
    url = USERSERVICE + "/users/register"
    params = {
        "username" : rider_name,
        "password" : rider_pass,
        "roles" : ["rider"]
    }
    response = requests.post(url=url,json=params)
    print(response.text)
    params["username"] = driver_name
    params["password"] = driver_pass
    params["roles"] = ["driver"]
    response = requests.post(url=url,json=params)
    print(response.text)

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
    data = requests.post(url=url,headers=headers,json=params)
    routeId = data.json()["route_id"]
    print(f"route id : {routeId}")
    return routeId


def register_arrival(jwt,dest,station):
    url = RIDERSERVICE + "/api/register-arrival"
    headers = {
        "Authorization": f"Bearer {jwt}",
        # "Content-Type": "application/json"
    }
    time = (datetime.datetime.now() + datetime.timedelta(minutes=4)).strftime("%Y-%m-%dT%H:%M:%S")
    params = {
        "arrivaltime" : time,
        "destination" : dest,
        "arrivalstationname" : station
    }
    data = requests.post(url=url,headers=headers,json=params)
    print(data.text)

def update_loc(jwt,routeid,loc):
    url = DRIVERSERVICE + "/api/update-location"
    headers = {
        "Authorization": f"Bearer {jwt}",
        "Content-Type": "application/json"
    }
    params = {
        "route_id" : routeid,
        "location" : loc
    }
    data = requests.patch(url=url,headers=headers,json=params)
    print(data.text)

def confirm_ride(jwt,tripid,arrivalid):
    url = TRIPSERVICE+"/api/confirmTrip"
    header={
        "Authorization":f"Bearer {jwt}",
        "Content-Type": "application/json"
    }
    param = {
        "tripId" : tripid,
        "arrivalId" :arrivalid
    }
    response = requests.post(url,json=param,headers=header)
    print(response.text)

dest = "marathalli"

register_a_rider_and_driver("abhi","abhi","sid","sid")
tokens = login("abhi","abhi","sid","sid")
route_id = register_route(tokens[1],"electronic_city",dest,4)
register_arrival(tokens[0],dest,"METROSTATION_SILKBOARD")
time.sleep(30)
update_loc(tokens[1],route_id,"hsrlayout")
time.sleep(30)
update_loc(tokens[1],route_id,"METROSTATION_SILKBOARD")
tripId=int(input("Enter TripId: "))
arrivalId=int(input("Enter ArrivalId: "))
confirm_ride(tokens[0],tripId,arrivalId)
time.sleep(30)
update_loc(tokens[1],route_id,dest)