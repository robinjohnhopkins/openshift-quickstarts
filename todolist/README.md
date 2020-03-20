## Part 1. JBoss Web Server 3.1 Apache Tomcat 8 + MySQL (with https) on OpenShift

https://www.youtube.com/watch?v=eE67hG1hUYg

Need more memory than default free tier to build

build spec
spec:
  resources:
    limits:
      cpu: "100m" 
      memory: “1000Mi"


created a web hook secret to see that http can work though https wont
jws-app-secret

http://jws-app-tomtest.apps.ca-central-1.starter.openshift-online.com/index.html

then gives a site that can add todo db entries and list those entries.

Create route
	name: ownhttps
	service: jws-app
	target port: 8080-8080
	secure root tick
	TLS termination: Edge
	insecure traffic: none
Then we have a secure site edge terminated

https://ownhttps-tomtest.apps.ca-central-1.starter.openshift-online.com/index.html

secure-jws-app-tomtest.apps.ca-central-1.starter.openshift-online.com
sr-tomtest.apps.ca-central-1.starter.openshift-online.com
123456789 123456789 123456789 123456789 123456789 123456789 12345 max 65 chars 

https://www.selfsignedcertificate.com/

once you have the self signed cert you base64 encode then alter the secret as follows

kind: Secret
apiVersion: v1
metadata:
  name: jws-app-secret
  namespace: tomtest
  selfLink: /api/v1/namespaces/tomtest/secrets/jws-app-secret
  uid: a5c35e04-6aa7-11ea-891b-02dc4547eb4e
  resourceVersion: '211951975'
  creationTimestamp: '2020-03-20T12:38:06Z'
data:
  server.crt: >-
    LS0tLS1CRUdJT........RE13VlQ3aEZKTUEwR0NTcUdTSWIzRFFFQkJRVUFNRVF4UWpCQUJnTlYKQkFNTU9YTn………o0Y2UyWS9ZdXdScVQxT0pEamNSN3doUGJxNVptNVhnMFFramp5cGZlcUNKWWVGbmZCMTQKb3YxUjhRUkM5eFdvRTVLaSt2SWtoUzJhR3cwTVkzWTR2NUUzbWZ5V2VaczBrM3llOUpkRjlwQ0FIM3JrcG9XWApBMEl2NG0rbzhNQytSZGVCZkFPTjMwcEx6MW9tYXhoNnRQaHRJSXk2UC9XRUR6M0J6dVNnL2dpV0trLytiNDg9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0=
  server.key: >-
    LS0tLS1CRUdJT......VmNyt6aW4vTW9JY2w0ZE1JRThGeTBESENPQmVYZEJwCkpDR………..AKeXYrZ0ZhVHJKN2RESHVqZFBSMVR3Q2puM1dCamdQUXhNcnFpNitlZGdZNVdQRDZtbjhBa0JkOHVzcmxhNWxpeApXSmtZSjluR1pZT3FjVHRmM1JVTUMzQVBFZ04rTkxhWkQ3U0o2cjJVa1ZLb3hPblN3QW5DCi0tLS0tRU5EIFJTQSBQUklWQVRFIEtFWS0tLS0t
type: Opaque

I also had to create a new route ensuring the generated name was not > 65 chars I chose sr prefix but needed to remove port and use secure-jws-app
 
spec:
  host: sr-tomtest.apps.ca-central-1.starter.openshift-online.com
  subdomain: ''
  to:
    kind: Service
    name: secure-jws-app
    weight: 100
  tls:
    termination: passthrough
  wildcardPolicy: None

That was enough to standup a connection through to
wget --no-check-certificate  https://sr-tomtest.apps.ca-central-1.starter.openshift-online.com/

however, safari and chrome on mac weren’t trusting which is a good thing
