## Part 1. JBoss Web Server 3.1 Apache Tomcat 8 + MySQL (with https) on OpenShift

https://www.youtube.com/watch?v=eE67hG1hUYg

On cloud openshift:

as Developer

create project

+Add

from catalogue

JBoss Web Server 3.1 Apache Tomcat 8 + MySQL (with https)




Need more memory than default free tier to build

```
build spec
spec:
  resources:
    limits:
      cpu: "100m" 
      memory: “1000Mi"
```

There is a requirement for secret jws-app-secret, without which openshift will not standup the app.

Create a web hook secret jws-app-secret (that wont be used) to see that the http route can work.
NB the https route will not work at this stage.


http://jws-app-tomtest.apps.ca-central-1.starter.openshift-online.com/index.html

then gives a site that can add todo db entries and list those entries.

```
Create route
	name: ownhttps
	service: jws-app
	target port: 8080-8080
	secure root tick
	TLS termination: Edge
	insecure traffic: none
```

Then we can create/have a secure site edge terminated route

https://ownhttps-tomtest.apps.ca-central-1.starter.openshift-online.com/index.html

We can also create an https route (ensuring total name length <= 65 chars)
Create a self signed cert for this.

```
# my first attempt failed as too long
secure-jws-app-tomtest.apps.ca-central-1.starter.openshift-online.com
# this one was better
sr-tomtest.apps.ca-central-1.starter.openshift-online.com
123456789 123456789 123456789 123456789 123456789 123456789 12345 max 65 chars 
```

https://www.selfsignedcertificate.com/

once you have the self signed cert you base64 encode then alter the secure-jws-app secret as follows

```
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
```

Incidentally, I also had to create a new route ensuring the generated name was not > 65 chars I chose sr prefix but needed to remove port and use secure-jws-app
 
```
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
```

That was enough to standup a connection through to

```
wget --no-check-certificate  https://sr-tomtest.apps.ca-central-1.starter.openshift-online.com/
```

however, safari and chrome on mac weren’t trusting which is a good thing
But it shows what you would need to do with a real cert.

## Add tag to commit point

```
git log --pretty=oneline
dba7e3a8b818a3dbf933672f79019e3fea32788b (HEAD -> master, origin/master, origin/HEAD) pom.xml
...

git tag -a startPoint 2d598ee51abff453baba7715510e5aec90e691dc
git tag -a restpointWorking
git push  --tags origin

```


## REST end point

Use above tags, to see code diff to get REST point working.

[compare](https://github.com/robinjohnhopkins/openshift-quickstarts/compare/startPoint...robinjohnhopkins:restpointWorking)

rest point is

https://ownhttps-tomtest.apps.ca-central-1.starter.openshift-online.com/rest/rest/numbers

return is:
```
[{"valueType":"STRING","string":"1584733946516","chars":"1584733946516"},{"valueType":"STRING","string":"1584733946516","chars":"1584733946516"},{"valueType":"STRING","string":"1584733946516","chars":"1584733946516"},{"valueType":"STRING","string":"1584733946516","chars":"1584733946516"},{"valueType":"STRING","string":"1584733946516","chars":"1584733946516"},{"valueType":"STRING","string":"1584733946516","chars":"1584733946516"},{"valueType":"STRING","string":"1584733946516","chars":"1584733946516"},{"valueType":"STRING","string":"1584733946516","chars":"1584733946516"},{"valueType":"STRING","string":"1584733946516","chars":"1584733946516"},{"valueType":"STRING","string":"1584733946516","chars":"1584733946516"}]
```

## deployment

```
kind: DeploymentConfig
apiVersion: apps.openshift.io/v1
metadata:
  name: jws-app
  namespace: tomtest
  selfLink: /apis/apps.openshift.io/v1/namespaces/tomtest/deploymentconfigs/jws-app
  uid: 113f8d8f-6aa3-11ea-951f-0a580a800009
  resourceVersion: '214020262'
  generation: 33
  creationTimestamp: '2020-03-20T12:05:18Z'
  labels:
    application: jws-app
    jws31: '1.4'
    template: jws31-tomcat8-mysql-persistent-s2i
    template.openshift.io/template-instance-owner: 10fc0556-6aa3-11ea-9e19-0a580a810002
spec:
  strategy:
    type: Recreate
    recreateParams:
      timeoutSeconds: 600
    resources: {}
    activeDeadlineSeconds: 21600
  triggers:
    - type: ImageChange
      imageChangeParams:
        automatic: true
        containerNames:
          - jws-app
        from:
          kind: ImageStreamTag
          namespace: tomtest
          name: 'jws-app:latest'
        lastTriggeredImage: >-
          image-registry.openshift-image-registry.svc:5000/tomtest/jws-app@sha256:42cd3f31adff4d049033ba2037a58d0b7793d41748c6d0817ab5d2eed8f9bbea
    - type: ConfigChange
  replicas: 1
  revisionHistoryLimit: 10
  test: false
  selector:
    deploymentConfig: jws-app
  template:
    metadata:
      name: jws-app
      creationTimestamp: null
      labels:
        application: jws-app
        deploymentConfig: jws-app
    spec:
      volumes:
        - name: jws-certificate-volume
          secret:
            secretName: jws-app-secret
            defaultMode: 420
      containers:
        - resources: {}
          readinessProbe:
            exec:
              command:
                - /bin/bash
                - '-c'
                - >-
                  curl --noproxy '*' -s -u BOjpmkLH:mr4pY65t
                  'http://localhost:8080/manager/jmxproxy/?get=Catalina%3Atype%3DServer&att=stateName'
                  |grep -iq 'stateName *= *STARTED'
            timeoutSeconds: 1
            periodSeconds: 10
            successThreshold: 1
            failureThreshold: 3
          terminationMessagePath: /dev/termination-log
          name: jws-app
          env:
            - name: DB_SERVICE_PREFIX_MAPPING
              value: jws-app-mysql=DB
            - name: DB_JNDI
              value: jboss/datasources/defaultDS
            - name: DB_USERNAME
              value: useryFs
            - name: DB_PASSWORD
              value: nB1l0nMb
            - name: DB_DATABASE
              value: root
            - name: DB_MIN_POOL_SIZE
            - name: DB_MAX_POOL_SIZE
            - name: DB_TX_ISOLATION
            - name: JWS_HTTPS_CERTIFICATE_DIR
              value: /etc/jws-secret-volume
            - name: JWS_HTTPS_CERTIFICATE
              value: server.crt
            - name: JWS_HTTPS_CERTIFICATE_KEY
              value: server.key
            - name: JWS_HTTPS_CERTIFICATE_PASSWORD
            - name: JWS_ADMIN_USERNAME
              value: BOjpmkLH
            - name: JWS_ADMIN_PASSWORD
              value: mr4pY65t
          ports:
            - name: jolokia
              containerPort: 8778
              protocol: TCP
            - name: http
              containerPort: 8080
              protocol: TCP
            - name: https
              containerPort: 8443
              protocol: TCP
          imagePullPolicy: Always
          volumeMounts:
            - name: jws-certificate-volume
              readOnly: true
              mountPath: /etc/jws-secret-volume
          terminationMessagePolicy: File
          image: >-
            image-registry.openshift-image-registry.svc:5000/tomtest/jws-app@sha256:42cd3f31adff4d049033ba2037a58d0b7793d41748c6d0817ab5d2eed8f9bbea
      restartPolicy: Always
      terminationGracePeriodSeconds: 60
      dnsPolicy: ClusterFirst
      securityContext: {}
      schedulerName: default-scheduler
status:
  observedGeneration: 33
  details:
    message: image change
    causes:
      - type: ImageChange
        imageTrigger:
          from:
            kind: DockerImage
            name: >-
              image-registry.openshift-image-registry.svc:5000/tomtest/jws-app@sha256:42cd3f31adff4d049033ba2037a58d0b7793d41748c6d0817ab5d2eed8f9bbea
  availableReplicas: 1
  unavailableReplicas: 0
  latestVersion: 12
  updatedReplicas: 1
  conditions:
    - type: Progressing
      status: 'True'
      lastUpdateTime: '2020-03-21T21:05:37Z'
      lastTransitionTime: '2020-03-21T21:05:07Z'
      reason: NewReplicationControllerAvailable
      message: replication controller "jws-app-12" successfully rolled out
    - type: Available
      status: 'True'
      lastUpdateTime: '2020-03-22T13:17:42Z'
      lastTransitionTime: '2020-03-22T13:17:42Z'
      message: Deployment config has minimum availability.
  replicas: 1
  readyReplicas: 1
```

http://jws-app-tomtest.apps.ca-central-1.starter.openshift-online.com/rest/rest/files?pattern=*.crt

```
{"files":[{"valueType":"STRING","chars":"/run/secrets/kubernetes.io/serviceaccount/ca.crt","string":"/run/secrets/kubernetes.io/serviceaccount/ca.crt"},{"valueType":"STRING","chars":"/run/secrets/kubernetes.io/serviceaccount/service-ca.crt","string":"/run/secrets/kubernetes.io/serviceaccount/service-ca.crt"},{"valueType":"STRING","chars":"/run/secrets/kubernetes.io/serviceaccount/..2020_03_22_13_17_26.920202511/service-ca.crt","string":"/run/secrets/kubernetes.io/serviceaccount/..2020_03_22_13_17_26.920202511/service-ca.crt"},{"valueType":"STRING","chars":"/run/secrets/kubernetes.io/serviceaccount/..2020_03_22_13_17_26.920202511/ca.crt","string":"/run/secrets/kubernetes.io/serviceaccount/..2020_03_22_13_17_26.920202511/ca.crt"},{"valueType":"STRING","chars":"/etc/pki/ca-trust/extracted/openssl/ca-bundle.trust.crt","string":"/etc/pki/ca-trust/extracted/openssl/ca-bundle.trust.crt"},{"valueType":"STRING","chars":"/etc/pki/ca-trust/source/ca-bundle.legacy.crt","string":"/etc/pki/ca-trust/source/ca-bundle.legacy.crt"},{"valueType":"STRING","chars":"/etc/pki/tls/certs/ca-bundle.crt","string":"/etc/pki/tls/certs/ca-bundle.crt"},{"valueType":"STRING","chars":"/etc/pki/tls/certs/ca-bundle.trust.crt","string":"/etc/pki/tls/certs/ca-bundle.trust.crt"},{"valueType":"STRING","chars":"/etc/jws-secret-volume/server.crt","string":"/etc/jws-secret-volume/server.crt"},{"valueType":"STRING","chars":"/etc/jws-secret-volume/..2020_03_22_13_17_26.465657122/server.crt","string":"/etc/jws-secret-volume/..2020_03_22_13_17_26.465657122/server.crt"},{"valueType":"STRING","chars":"/usr/share/pki/ca-trust-legacy/ca-bundle.legacy.default.crt","string":"/usr/share/pki/ca-trust-legacy/ca-bundle.legacy.default.crt"},{"valueType":"STRING","chars":"/usr/share/pki/ca-trust-legacy/ca-bundle.legacy.disable.crt","string":"/usr/share/pki/ca-trust-legacy/ca-bundle.legacy.disable.crt"}]}
```

secret defined as two base64 encoded strings deployed as files
to mountPath: /etc/jws-secret-volume

see above

```
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
    LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSURXekNDQWtPZ0F3SUJBZ0lKQUlGRE13VlQ3aEZKTUEwR0NTcUdTSWIzRFFFQkJRVUFNRVF4UWpCQUJnTlYKQkFNTU9YTnlMWFJ2YlhSbGMzUXVZWEJ3Y3k1allTMWpaVzUwY21Gc0xURXVjM1JoY25SbGNpNXZjR1Z1YzJocApablF0YjI1c2FXNWxMbU52YlRBZUZ3MHlNREF6TWpBeE16VTJNRFJhRncwek1EQXpNVGd4TXpVMk1EUmFNRVF4ClFqQkFCZ05WQkFNTU9YTnlMWFJ2YlhSbGMzUXVZWEJ3Y3k1allTMWpaVzUwY21Gc0xURXVjM1JoY25SbGNpNXYKY0dWdWMyaHBablF0YjI1c2FXNWxMbU52YlRDQ0FTSXdEUVlKS29aSWh2Y05BUUVCQlFBRGdnRVBBRENDQVFvQwpnZ0VCQUt1SGR5VmdGVU5vK0dkdjV1WCsvczRwL3pLQ0hKZUhUQ0JQQmN0QXh3amdYbDNRYVNRaGdhZStYaDVUCkYwME91c2NaK0pndHFTQkc1ZkZpZi82bWo4WEVwYjFUbEdqb0JBV29YMFZqYlFBS1czaFl6WkxZdVRLcWZRZ3cKdk1OZ1hsa2xIWWpuNy9hN2ErRDV3OHFzVkJPMXhUZmh1MUhHeVA5V1NVZzdZeE9SdU8yTERDMmM3S2g1dGZZWgpxRkJwWnBja2p2Zk1aaVhCNjY1RGxTVkhrTFBLcTkvSUljWUJkMGs2Y2ZrZUxOQ3lxR0JZRHFxQUdMd3V1NFNnCkVFemY0emx2OUpNaHV5WGlNWmRTS3pFcDFJMzcva0R2b0hkeWlDaDlxeVBvdFNiNnBqUVpIQkkweXlKSXY3cFAKNlRLbDU5NCsvYm0vZHdtTmVzd2QxNThVQm5rQ0F3RUFBYU5RTUU0d0hRWURWUjBPQkJZRUZHK254Rk1HVmFsNgpuT1FOekxQd2pPTmREWHp1TUI4R0ExVWRJd1FZTUJhQUZHK254Rk1HVmFsNm5PUU56TFB3ak9OZERYenVNQXdHCkExVWRFd1FGTUFNQkFmOHdEUVlKS29aSWh2Y05BUUVGQlFBRGdnRUJBQTR5SDgvbGRMbHlHMXo2SUlqaklzeDkKeDBEVGVxVThheFhHcEdZREhyaUZYRjVCakNPSG40SjNhbzA2Q1ZHaUNyN0RMN1RqblZCQ1FvRXNpVDNkcEU5aApvelhQVXcvMTRlVjdNRDJZWTFsUUxLVXpiZ3IybEVLMGlydHE4aWlzTmhJMWx4aGF1eDAvL0piR3doT1lFdXlICldSQWZyNHpaWXNRcno0Y2UyWS9ZdXdScVQxT0pEamNSN3doUGJxNVptNVhnMFFramp5cGZlcUNKWWVGbmZCMTQKb3YxUjhRUkM5eFdvRTVLaSt2SWtoUzJhR3cwTVkzWTR2NUUzbWZ5V2VaczBrM3llOUpkRjlwQ0FIM3JrcG9XWApBMEl2NG0rbzhNQytSZGVCZkFPTjMwcEx6MW9tYXhoNnRQaHRJSXk2UC9XRUR6M0J6dVNnL2dpV0trLytiNDg9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0=
  server.key: >-
    LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlFb3dJQkFBS0NBUUVBcTRkM0pXQVZRMmo0WjIvbTVmNyt6aW4vTW9JY2w0ZE1JRThGeTBESENPQmVYZEJwCkpDR0JwNzVlSGxNWFRRNjZ4eG40bUMycElFYmw4V0ovL3FhUHhjU2x2Vk9VYU9nRUJhaGZSV050QUFwYmVGak4Ka3RpNU1xcDlDREM4dzJCZVdTVWRpT2Z2OXJ0cjRQbkR5cXhVRTdYRk4rRzdVY2JJLzFaSlNEdGpFNUc0N1lzTQpMWnpzcUhtMTlobW9VR2xtbHlTTzk4eG1KY0hycmtPVkpVZVFzOHFyMzhnaHhnRjNTVHB4K1I0czBMS29ZRmdPCnFvQVl2QzY3aEtBUVROL2pPVy8wa3lHN0plSXhsMUlyTVNuVWpmditRTytnZDNLSUtIMnJJK2kxSnZxbU5Ca2MKRWpUTElraS91ay9wTXFYbjNqNzl1YjkzQ1kxNnpCM1hueFFHZVFJREFRQUJBb0lCQUFsdmNURmdJS3RObWVSdQp0bC9PY0F1OUthbUltTFFKSURuaXZ1bllUVnFDcFE0NkNaNlFMVWFoNVNGNzVwczVoVEt3amdrbzhkV3hzTEpmCmZiVHZTcGRSWXVDUU9XaXY0cERCRzBTQTNKMFU5TVVnOVdYdmYvaDJnbHd4K2lBeEs5OXhlRGR3c3JWNng4dGYKVWRMVE9laS9wZktWK3l6YlozRU1PUW05SW8wUDd1Z2RYTGlLSzB3cmIyQ21KMU5ZVDhsVnZpUm9BR1VIZTZMYwpuVjJ0aGRKelhqS2JiMW5PcGdMWUQrS1V4SWNNekRSY1RqYmtLY0diL3BQaVJZV2xtaG5jL29ZcXBOSFRKYlVjCmRqVHlGbHZwcVVrSFRNSkVER2dhTjNWbitxQkZuOXBwWG9HL1hXbW5LZ3dwMDR6SlVZSHljeFVDZ04wOXRXRkMKcDEzZUhIVUNnWUVBM3U5WEprdkpwZWdVemhPUWZZQVFRUm5WSVNpZnRtSVhMUTh3OWVXa0VPNC9kYmpNUndVbQo5anp6QlVGMzdseWNRdnBvMmlaTDl3YjdYN29Eb24yR0dENlkraDRoT01PM0V3cVNISXRieTV3Wms3cmFseUF3CjJpVWpTUVNscXlTZXowVGJEdWtUMUJKdTFSNzRrNlo3emdGWC9GMjBYTUk5OGVvemR6OEJRYk1DZ1lFQXhQaEsKSXZXM2MzY2cxa1NRK3Z5dmp5NWd1TnZnSmpZQmVZQnkwK3RJbzM0cHFsU1prNEszdUl0MUNLK21UcWhDTVZXMQpFM3RNM25tamk0VStHSFZ3clVONmtFZ3VZbVVDNDNMMTd1Qk1XREhkZ0k5RnNmU1FHY3loRW1iaDVFaWtzSU9rCmh6a0xiOXBXZmx4S2dGTVVhOTBNSkl5VEI0eW1NbVE0bG5Na1NTTUNnWUI3UjUxSTF5WW1jYVYzcEFnRjdwaU8KZDR1TmI1cUtUOG9ESitCcVNsZ2s2S3I5SFhrUldQOEhzZ0t0aUZ1YTIybXAvREdaV1E0dkI1Umdoa3ZXUWxXeAorSU85NUlWNGozb2d0SHJDQUNwOStNK0lDdzh2L1NRbXJkektWeUNKK0pFOWl4VWNOTFc3VTRvb25welZnWDM3CkdIRWdaRzBGMG9CUTA3TXNJUkkvMHdLQmdRQ2xNN1pDZ2lKK2hxRVU5V2RBQzJWWHlzYzI1THVvY00xbDBkVDkKWnk2Q2FkT2FWK0NETnpUT1pMRWhPdWxhdVliakNBYWFzMlN5aUFVaWhQdWkyZ1ZLWFBsVVFDWWZRcWRjaTFRNApLUW4vUXJ1TUV4NmFkZ3ZTaDYxazJNT0hpMklONWl1OWVwdmNFWjFQMkNwb1hNWVkvY29zY1hiejh3U0Y2VWNJCjhNT3pUd0tCZ0VtTXlMWWtvVUZaSEpZdDNGYmYydW9YbVZmWHRwaXpqSWYyd0ZIMVBvZ3FaSWRPcU9uNXZTMDAKeXYrZ0ZhVHJKN2RESHVqZFBSMVR3Q2puM1dCamdQUXhNcnFpNitlZGdZNVdQRDZtbjhBa0JkOHVzcmxhNWxpeApXSmtZSjluR1pZT3FjVHRmM1JVTUMzQVBFZ04rTkxhWkQ3U0o2cjJVa1ZLb3hPblN3QW5DCi0tLS0tRU5EIFJTQSBQUklWQVRFIEtFWS0tLS0t
type: Opaque
```

## create keystore from pems:

```
      pwd
      #$  ~/workspace/openshift-quickstarts/openshift-quickstarts/todolist
      openssl pkcs12 -export -in ../../sr-tomtest.apps.ca-central-1.starter.openshift-online.com.cert -inkey ../../sr-tomtest.apps.ca-central-1.starter.openshift-online.com.key -certfile ../../sr-tomtest.apps.ca-central-1.starter.openshift-online.com.cert -out keystore.p12  

      keytool -importkeystore -srckeystore keystore.p12 -srcstoretype pkcs12 -destkeystore keystore.jks -deststoretype JKS
```

## Run standalone server using potentially self signed certs that rest call can connect to

You will have to open up a port to forward in your router if running locally.

```
java -cp  /Users/robinjohnhopkins/.m2/repository/org/bouncycastle/bcprov-jdk15on/1.64/bcprov-jdk15on-1.64.jar:cmdlineserver/target/cmdlineserver.jar org.openshift.quickstarts.todolist.stuff.ClassFileServer 32000 . TLS true /Users/robinjohnhopkins/workspace/openshift-quickstarts/openshift-quickstarts/todolist/keystore.jks password
```

Also ensure certs are available on docker instance and declare env variables that rest call uses to secure outgoing connection:

CACERTFILENAME=/etc/jws-secret-volume/server.crt
CLIENTCERTFILENAME=/etc/jws-secret-volume/server.crt
CLIENTKEYFILENAME/etc/jws-secret-volume/server.key

This test was done with self signed certs used by both sender and destination.
But obviously can be used with real certs. :)

Then the test url is

http://jws-app-tomtest.apps.ca-central-1.starter.openshift-online.com/rest/ssl/call?host=<routerip>&port=32000&file=myfile

The default file is README.md and this must be present in the dir that above ClassFileServer is running from.
The content of the file is returned.
e.g.

```
{"result":{"valueType":"STRING","string":"HTTP/1.0 200 OKContent-Length: 16379Content-Type: text/html## Part 1. JBoss Web Server 3.1 Apache Tomcat 8 + MySQL 
...
```

## What we have done
We have explored running wildfly app in online openshift with differing SSL.
Additionally we have added a test end point that makes an outgoing SSL connection.
Thus two way SSL with not necessarily the same cert used to stand up the server as used in the outgoing connection.


## Modify build and deploy

In src see
```
  todolist-jdbc/.s2i/bin/assemble
  
in log:

    STEP 9: RUN /tmp/scripts/assemble
    ====== Before assembling
    /usr/libexec/s2i/assemble is dir in SOME s2i but real openshift 4 is /usr/local/s2i/assemble


    INFO Copying deployments from target to /deployments...
    INFO Copying configuration from .s2i/bin to /opt/webserver/conf...
    INFO S2I_TARGET_DATA_DIR does not exist, creating /opt/webserver/data
    INFO Copying app data from data to /opt/webserver/data...
    mydata.txt
    INFO Copying deployments from deployments to /deployments...
    '/tmp/src/deployments/ROOT.war' -> '/deployments/ROOT.war'
    ====== After successful assembling
```

for ability to modify assembly scripts!

similarly we can alter the run, potentially with some pre-configuration then run intended run script

```
  todolist-jdbc/.s2i/bin/run
  
  
in log:
    Before running application
    exec /usr/libexec/s2i/run is run script on SOME openshift images, However
    from build log we see real openshift run script is: STEP 9: CMD /usr/local/s2i/run  
```

I have also added a data dir which can be seen from a terminal in the running container:

```
    cat /opt/webserver/data/mydata.txt 
    my data
```

For tomcat apache then this dir and xml is key and could be tweaked on startup.

/opt/webserver/conf/server.xml

The tweaked startup scripts could potentially do something with files in conf dir.
 
/tmp/src/conf/myconf.txt


