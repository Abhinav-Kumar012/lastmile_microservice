from locust import HttpUser, task, between
import requests
import time

INGRESS_PORT = 80
MINIKUBE_IP = "192.168.49.2"

REGISTER_URL = f"http://{MINIKUBE_IP}:{INGRESS_PORT}/users/register"
LOGIN_URL = f"http://{MINIKUBE_IP}:{INGRESS_PORT}/users/login"
USERNAME = "loadtest_user"
PASSWORD = "loadtest_pass"
ROLES = ["rider"]
ROLE = "rider"

def login(user_name : str,user_pass: str,role : str,url : str) -> str:
    params = {
        "username" : user_name,
        "password" : user_pass,
        "role" : role
    }
    response = requests.post(url=url,json=params)
    return response.json().get("token")


def register(user_name : str,user_pass : str,roles : list[str],url : str):
    params = {
        "username" : user_name,
        "password" : user_pass,
        "roles" : roles
    }
    response = requests.post(url=url,json=params)
    print(f"user {user_name} : {response.text}")

class LoginLoadTest(HttpUser):
    host = f"http://{MINIKUBE_IP}"
    wait_time = between(1, 2)

    def on_start(self):
        try:
            register(USERNAME, PASSWORD, ROLES, REGISTER_URL)
        except Exception as e:
            print("User may already exist:", e)

    @task
    def login_test(self):
        start_time = time.time()

        try:
            token = login(USERNAME, PASSWORD, ROLE, LOGIN_URL)

            if not token:
                raise Exception("Empty token returned")

            # success
            total_time = (time.time() - start_time) * 1000

            self.environment.events.request.fire(
                request_type="CUSTOM",
                name="login",
                response_time=total_time,
                response_length=len(token),
                exception=None,
                context=self,
            )

        except Exception as e:
            # failure
            total_time = (time.time() - start_time) * 1000

            self.environment.events.request.fire(
                request_type="CUSTOM",
                name="login",
                response_time=total_time,
                response_length=0,
                exception=e,
                context=self,
            )
