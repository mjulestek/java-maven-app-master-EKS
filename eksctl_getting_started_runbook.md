# EKS with `eksctl` Runbook — Installation + YAML Cluster Creation

This runbook is for creating an AWS EKS cluster from Ubuntu WSL using `eksctl`.

Main flow:

```text
Install tools → configure AWS → create EKS cluster → connect kubectl → verify → delete when finished
```

Important WSL rule:

```text
Line continuation with \ is correct in Ubuntu WSL.
Use normal double hyphens --, not long dashes —.
```

---

## 0. Before You Start

You need:

```text
Ubuntu WSL
AWS account
AWS Access Key ID
AWS Secret Access Key
AWS CLI permissions for EKS, EC2, IAM, and CloudFormation
```

For this learning demo:

```text
Cluster name: jennifer-demo-cluster
Region: eu-central-1
Kubernetes version: 1.30
Node group name: jennifer-demo-nodes
Node type: t3.medium
Desired nodes: 2
Minimum nodes: 1
Maximum nodes: 3
```

Why `t3.medium`, not `t2.micro`?

```text
EKS needs enough memory for Kubernetes system pods.
t2.micro is usually too small and can cause scheduling issues.
t3.medium is smoother for a demo.
```

---

# Part 1 — Install Required Tools

## 1. Install required basics

```bash
sudo apt update
sudo apt install -y curl tar gzip unzip
```

---

## 2. Install `eksctl` from official GitHub releases

`eksctl` is the command-line tool we use to create and manage EKS clusters.

```bash
curl --silent --location "https://github.com/eksctl-io/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin
```

Verify:

```bash
eksctl version
```

---

## 3. Install `kubectl`

`kubectl` is the Kubernetes command-line tool. We use it to talk to the EKS cluster.

```bash
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
chmod +x kubectl
sudo mv kubectl /usr/local/bin/
```

Verify:

```bash
kubectl version --client
```

---

## 4. Install AWS CLI v2

AWS CLI allows your terminal to communicate with AWS services.

```bash
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

Verify:

```bash
aws --version
```

---

# Part 2 — Configure AWS Access

## 5. Configure AWS credentials

```bash
aws configure
```

Enter:

```text
AWS Access Key ID: your-new-access-key
AWS Secret Access Key: your-new-secret-key
Default region name: eu-central-1
Default output format: json
```

Verify your AWS identity:

```bash
aws sts get-caller-identity
```

Expected example:

```json
{
    "UserId": "AIDA...",
    "Account": "123456789012",
    "Arn": "arn:aws:iam::123456789012:user/your-user"
}
```

If this works, your AWS CLI is authenticated.

---

# Part 3 — Create EKS Cluster

There are two ways to create the EKS cluster:

```text
Method A: Direct terminal command
Method B: YAML file approach
```

For learning and DevOps notes, the YAML method is better because the cluster configuration is saved in a file.

---

## Method A — Direct `eksctl` Command

```bash
eksctl create cluster \
  --name jennifer-demo-cluster \
  --version 1.30 \
  --region eu-central-1 \
  --nodegroup-name jennifer-demo-nodes \
  --node-type t3.medium \
  --nodes 2 \
  --nodes-min 1 \
  --nodes-max 3 \
  --managed
```

This can take around 15–25 minutes.

Explanation:

```text
--name
The EKS cluster name.

--version
The Kubernetes version.

--region
The AWS region where the cluster will be created.

--nodegroup-name
The name of the worker node group.

--node-type
The EC2 instance type used as Kubernetes worker nodes.

--nodes
How many worker nodes to start with.

--nodes-min
Minimum number of nodes.

--nodes-max
Maximum number of nodes.

--managed
Creates a managed node group. AWS manages the node group lifecycle better than self-managed nodes.
```

---

## Method B — YAML File Approach

This is the recommended method for learning.

Instead of writing a long command, you describe the cluster in a YAML file.

Create the file:

```bash
nano cluster.yaml
```

Paste this:

```yaml
# cluster.yaml
# EKS cluster for Jenkins CD demo

apiVersion: eksctl.io/v1alpha5
kind: ClusterConfig

metadata:
  name: jennifer-demo-cluster
  region: eu-central-1
  version: "1.30"

managedNodeGroups:
  - name: jennifer-demo-nodes
    instanceType: t3.medium
    desiredCapacity: 2
    minSize: 1
    maxSize: 3
    volumeSize: 20
    labels:
      role: worker
    tags:
      nodegroup-role: worker
    ssh:
      allow: true
```

Save the file:

```text
CTRL + O
Enter
CTRL + X
```

### YAML Explanation

This part defines the cluster:

```yaml
metadata:
  name: jennifer-demo-cluster
  region: eu-central-1
  version: "1.30"
```

Meaning:

```text
name: cluster name
region: AWS region
version: Kubernetes version
```

This part defines the worker nodes:

```yaml
managedNodeGroups:
  - name: jennifer-demo-nodes
    instanceType: t3.medium
    desiredCapacity: 2
    minSize: 1
    maxSize: 3
```

Meaning:

```text
managedNodeGroups:
Creates EC2 worker nodes managed by AWS.

desiredCapacity: 2
Start with 2 worker nodes.

minSize: 1
Autoscaling should not go below 1 node.

maxSize: 3
Autoscaling can go up to 3 nodes.

instanceType: t3.medium
Each worker node will be a t3.medium EC2 instance.
```

This part adds labels and tags:

```yaml
labels:
  role: worker
tags:
  nodegroup-role: worker
```

Meaning:

```text
labels:
Kubernetes labels added to nodes.

tags:
AWS tags added to AWS resources.
```

This part allows SSH:

```yaml
ssh:
  allow: true
```

For learning, this is okay. In production, many teams avoid SSH and use AWS Systems Manager instead.

---

## Dry Run First

A dry run shows what `eksctl` plans to create without creating real AWS resources.

```bash
eksctl create cluster -f cluster.yaml --dry-run
```

If everything looks good, create the cluster:

```bash
eksctl create cluster -f cluster.yaml
```

This can take around 15–25 minutes.

---

# Part 4 — Connect `kubectl` to the Cluster

After the cluster is created, connect your local `kubectl` to EKS:

```bash
aws eks update-kubeconfig \
  --region eu-central-1 \
  --name jennifer-demo-cluster
```

This updates your local kubeconfig file so `kubectl` knows how to connect to your EKS cluster.

Verify:

```bash
kubectl get nodes
```

Expected:

```text
NAME                                           STATUS   ROLES    AGE   VERSION
ip-...eu-central-1.compute.internal            Ready    <none>   ...   ...
```

---

# Part 5 — Check Cluster Resources

Check all system pods:

```bash
kubectl get pods -A
```

Check services:

```bash
kubectl get svc -A
```

Check node details:

```bash
kubectl get nodes -o wide
```

Check current Kubernetes context:

```bash
kubectl config current-context
```

List all contexts:

```bash
kubectl config get-contexts
```

---

# Part 6 — Useful `eksctl` Commands

List EKS clusters in a region:

```bash
eksctl get cluster --region eu-central-1
```

List node groups:

```bash
eksctl get nodegroup \
  --cluster jennifer-demo-cluster \
  --region eu-central-1
```

Scale node group:

```bash
eksctl scale nodegroup \
  --cluster jennifer-demo-cluster \
  --region eu-central-1 \
  --name jennifer-demo-nodes \
  --nodes 3 \
  --nodes-min 1 \
  --nodes-max 3
```

---

# Part 7 — Delete Everything Later

When you finish the demo and want to stop AWS costs, delete the cluster.

## If you created the cluster with the direct command

```bash
eksctl delete cluster \
  --name jennifer-demo-cluster \
  --region eu-central-1
```

## If you created the cluster with YAML

```bash
eksctl delete cluster -f cluster.yaml
```

This deletes:

```text
EKS cluster
Managed node group
EC2 worker nodes
CloudFormation stacks created by eksctl
Security groups created by eksctl
Most related EKS resources created by eksctl
```

---

## Clean Local Kubeconfig Context If Needed

Check contexts:

```bash
kubectl config get-contexts
```

Delete the matching context:

```bash
kubectl config delete-context CONTEXT_NAME
```

---

# Part 8 — Cost Reminder

EKS is not free.

Resources that can cost money:

```text
EKS control plane
EC2 worker nodes
EBS volumes
LoadBalancers
NAT gateways, if created
```

For learning:

```text
Create the cluster
Complete the demo
Take screenshots and notes
Delete the cluster
Verify it is gone
```

Verify deletion:

```bash
eksctl get cluster --region eu-central-1
aws eks list-clusters --region eu-central-1
```

---

# Part 9 — Common Mistakes

## Mistake 1: Using long dashes

Wrong:

```bash
eksctl create cluster \
  —name jennifer-demo-cluster
```

Correct:

```bash
eksctl create cluster \
  --name jennifer-demo-cluster
```

## Mistake 2: Using tiny nodes

Avoid:

```text
t2.micro
```

Use for demo:

```text
t3.medium
```

## Mistake 3: Creating cluster before AWS CLI works

Always test this first:

```bash
aws sts get-caller-identity
```

## Mistake 4: Forgetting to delete the cluster

Always delete when done:

```bash
eksctl delete cluster -f cluster.yaml
```

---

# Part 10 — Recommended Learning Flow

Use this order:

```text
1. Install tools
2. Configure AWS CLI
3. Verify AWS identity
4. Create cluster.yaml
5. Dry run
6. Create cluster
7. Connect kubectl
8. Verify nodes and pods
9. Continue with Jenkins deployment demo
10. Delete cluster when finished
```

Recommended command flow:

```bash
aws sts get-caller-identity

eksctl create cluster -f cluster.yaml --dry-run

eksctl create cluster -f cluster.yaml

aws eks update-kubeconfig \
  --region eu-central-1 \
  --name jennifer-demo-cluster

kubectl get nodes
kubectl get pods -A
```

---

# Final Rule

For this learning path, prefer the YAML approach:

```bash
eksctl create cluster -f cluster.yaml
```

Why?

```text
It is cleaner.
It is reusable.
It is easier to document.
It is closer to real DevOps practice.
It keeps your cluster configuration visible in Git.
```
