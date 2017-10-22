#!/bin/bash
cat <<YAML
apiVersion: apps/v1beta1
kind: Deployment
metadata:
  name: grpc-voting
spec:
  replicas: 1
  template:
    metadata:
      labels:
        app: grpc-voting
    spec:
      containers:
        - name: grpc-voting
          image: gcr.io/alien-fold-180922/grpc-voting:latest
          imagePullPolicy: Always
          ports:
            - containerPort: 8080
          env:
            - name: leaderboard_host
              value: "leaderboard"
            - name: leaderboard_port
              value: "8090"
            - name: counter
              value: "$(date +%s)"
---
apiVersion: v1
kind: Service
metadata:
  name: grpc-voting
spec:
  type: LoadBalancer
  selector:
    app: grpc-voting
  ports:
   - port: 8080
     targetPort: 8080
     protocol: TCP
YAML
