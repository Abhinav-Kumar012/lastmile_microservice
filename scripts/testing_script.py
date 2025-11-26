import requests
import datetime
import time

def register(user_name : str,user_pass : str,roles : list[str],url : str):
    params = {
        "username" : user_name,
        "password" : user_pass,
        "roles" : roles
    }
    response = requests.post(url=url,json=params)
    print(f"user {user_name} : {response.text}")

def register_both(rider_name : str,rider_pass : str,driver_name : str,driver_pass : str,url : str):
    register(rider_name,rider_pass,["rider"],url)
    register(driver_name,driver_pass,["driver"],url)

def login(user_name : str,user_pass: str,role : str,url : str) -> str:
    params = {
        "username" : user_name,
        "password" : user_pass,
        "role" : role
    }
    response = requests.post(url=url,json=params)
    return response.json().get("token")

def login_both(rider_name : str,rider_pass : str,driver_name : str,driver_pass : str,url : str) -> tuple[str,str]:
    rider_jwt = login(rider_name,rider_pass,"rider",url)
    driver_jwt = login(driver_name,driver_pass,"driver",url)
    print(f"{rider_jwt=}")
    print(f"{driver_jwt=}")
    return (rider_jwt,driver_jwt)
    

def register_route(jwt_token : str,starting : str,dest,seats : int,url : str) -> int:
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


def register_arrival(jwt : str,dest : str,station : str,url : str):
    headers = {
        "Authorization": f"Bearer {jwt}",
        # "Content-Type": "application/json"
    }
    time = (datetime.datetime.now(datetime.timezone.utc) + datetime.timedelta(minutes=4)).strftime("%Y-%m-%dT%H:%M:%S")
    params = {
        "arrivaltime" : time,
        "destination" : dest,
        "arrivalstationname" : station
    }
    data = requests.post(url=url,headers=headers,json=params)
    print(data.text)

def update_loc(jwt : str,routeid : int,loc : str,url : str):
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

def confirm_ride(jwt : str,tripid : int,arrivalid : int,url : str):
    header={
        "Authorization":f"Bearer {jwt}",
        "Content-Type": "application/json"
    }
    param = {
        "tripId" : tripid,
        "arrivalId" : arrivalid
    }
    response = requests.post(url,json=param,headers=header)
    print(response.text)

def main():
    rider_name = "abhinav"
    rider_pass = "abhinav"
    driver_name = "siddhesh"
    driver_pass = "siddhesh"
    destination = "marathalli"
    station = "METROSTATION_SILKBOARD"
    #inputs
    minikube_ip = input("enter ip of minikube : ")
    user_port = int(input("enter port of user-service : "))
    rider_port = int(input("enter port of rider-service : "))
    driver_port = int(input("enter port of driver-service : "))
    trip_port = int(input("enter port of trip-service : "))
    #hosts
    user_service_host = f"http://{minikube_ip}:{user_port}"
    rider_service_host = f"http://{minikube_ip}:{rider_port}"
    driver_service_host = f"http://{minikube_ip}:{driver_port}"
    trip_service_host = f"http://{minikube_ip}:{trip_port}"
    #urls
    url_register = user_service_host + "/users/register"
    url_login = user_service_host + "/users/login"
    route_url = driver_service_host + "/api/register-route"
    url_arrival = rider_service_host + "/api/register-arrival"
    update_loc_url = driver_service_host + "/api/update-location"
    conf_trip_url = trip_service_host + "/api/confirmTrip"

    register_both(rider_name,rider_pass,driver_name,driver_pass,url_register)
    rider_jwt, driver_jwt = login_both(rider_name,rider_pass,driver_name,driver_pass,url_login)
    route_id = register_route(driver_jwt,"electropnic_city",destination,4,route_url)
    register_arrival(rider_jwt,destination,station,url_arrival)
    time.sleep(30)
    update_loc(driver_jwt,route_id,"hsrlayout",update_loc_url)
    time.sleep(30)
    update_loc(driver_jwt,route_id,station,update_loc_url)
    tripId=int(input("Enter TripId : "))
    arrivalId=int(input("Enter ArrivalId : "))
    confirm_ride(rider_jwt,tripId,arrivalId,conf_trip_url)
    time.sleep(30)
    update_loc(driver_jwt,route_id,destination,update_loc_url)


if __name__=="__main__":
    main()