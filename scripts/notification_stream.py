import requests
from sseclient import SSEClient

url = "http://localhost:8086/notifications/stream"
role = input("enter the role: ")
jwt=input("Enter the Jwt Token: ")
header={
    "Authorization":f"Bearer {jwt}",
}
with requests.get(url, headers=header, stream=True) as response:
    if response.status_code != 200:
            print("Failed to connect:", response.status_code)
            exit(0)

    client = SSEClient(response)
    for event in client.events():
        print(f"[{role}] : {event.data}")


