# Jenkins to AWS ECR and EKS CI/CD Pipeline

This project demonstrates a complete CI/CD pipeline that builds, packages, pushes, and deploys a Java Maven application to AWS EKS using Jenkins, Docker, AWS ECR, and Kubernetes.

## Architecture

![CI/CD Architecture](screenshots/architecture1.jpg)

## Project Flow

```text
Developer pushes code to GitHub
        ↓
GitHub webhook triggers Jenkins
        ↓
Jenkins runs the pipeline
        ↓
Maven builds the Java application
        ↓
Docker builds the image
        ↓
Jenkins pushes the image to AWS ECR
        ↓
Jenkins deploys the app to AWS EKS
        ↓
Kubernetes runs the application
        ↓
LoadBalancer exposes the app to the browser