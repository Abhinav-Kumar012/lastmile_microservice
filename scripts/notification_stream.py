import asyncio
from aiohttp_sse_client import client as sse_client

async def listen_sse(url : str, jwt : str, role : str):
    headers = {"Authorization": f"Bearer {jwt}"}
    async with sse_client.EventSource(url, headers=headers) as event_source:
        async for event in event_source:
            print(f"[{role}] : {event.data}")

def main():
    minkube_ip = input("enter minikube ip : ").strip()
    notification_port = int(input("enter port of notification service : "))
    
    url = f"http://{minkube_ip}:{notification_port}/notifications/stream"
    role = input("Enter the role: ").strip()
    jwt = input("Enter the Jwt Token: ").strip()
    asyncio.run(listen_sse(url, jwt, role))

if __name__ == "__main__":
    main()



