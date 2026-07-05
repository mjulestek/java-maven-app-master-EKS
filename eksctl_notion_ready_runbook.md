# 🚀 EKS with `eksctl` — Beginner-Friendly Runbook

> **Goal:** Install the required tools, configure AWS access, create an EKS cluster using `eksctl`, connect with `kubectl`, verify the cluster, and delete everything later to avoid AWS costs.

---

## 📌 Main Learning Flow

```text
Install tools
→ Configure AWS CLI
→ Create EKS cluster
→ Connect kubectl
→ Verify cluster
→ Continue Jenkins deployment demo
→ Delete cluster when finished
```

---

## ⚠️ Important WSL Rule

Since we are using **Ubuntu WSL**, this line continuation is correct:

```bash
\
```

Example:

```bash
eksctl create cluster \
  --name my-cluster \
  --region eu-central-1
```

But always use normal double hyphens:

```text
--name
--region
--nodes
```

Do **not** use long dashes:

```text
—name ❌
—region ❌
```

---

# 0. Before You Start

## ✅ You Need

```text
Ubuntu WSL
AWS account
AWS Access Key ID
AWS Secret Access Key
AWS CLI permissions for EKS, EC2, IAM, and CloudFormation
Internet connection
```

---

## 🧠 Demo Cluster Information

For this learning demo, we will use:

| Item | Value |
|---|---|
| Cluster name | `jennifer-demo-cluster` |
| Region | `eu-central-1` |
| Kubernetes version | `1.30` |
| Node group name | `jennifer-demo-nodes` |
| Node type | `t3.medium` |
| Desired nodes | `2` |
| Minimum nodes | `1` |
| Maximum nodes | `3` |

---

## ❓ Why `t3.medium` and not `t2.micro`?

`EKS` needs enough memory to run Kubernetes system pods and your application pods.

```text
t2.micro is usually too small for EKS.
t3.medium is smoother for learning and demo projects.
```

---

# 1. Install Required Basics

Run:

```bash
sudo apt update
sudo apt install -y curl tar gzip unzip
```

## What these packages are for

| Package | Purpose |
|---|---|
| `curl` | Downloads files from the internet |
| `tar` | Extracts `.tar.gz` files |
| `gzip` | Handles compressed files |
| `unzip` | Extracts `.zip` files |

---

# 2. Install `eksctl`

## What is `eksctl`?

`eksctl` is a command-line tool used to create and manage AWS EKS clusters.

It helps create:

```text
EKS control plane
Worker nodes
Node groups
IAM roles
CloudFormation stacks
Networking resources
```

---

## Install `eksctl` from official GitHub releases

```bash
curl --silent --location "https://github.com/eksctl-io/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin
```

---

## Verify installation

```bash
eksctl version
```

Expected output example:

```text
0.xxx.x
```

If you see a version number, `eksctl` is installed successfully.

---

# 3. Install `kubectl`

## What is `kubectl`?

`kubectl` is the Kubernetes command-line tool.

We use it to interact with our EKS cluster:

```text
Check nodes
Check pods
Create deployments
Apply YAML files
Check services
Debug Kubernetes resources
```

---

## Install `kubectl`

```bash
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
chmod +x kubectl
sudo mv kubectl /usr/local/bin/
```

---

## Verify installation

```bash
kubectl version --client
```

If you see the client version, `kubectl` is installed correctly.

---

# 4. Install AWS CLI v2

## What is AWS CLI?

AWS CLI allows your terminal to communicate with AWS services.

We need it to:

```text
Authenticate to AWS
Verify AWS identity
Connect kubectl to EKS
Update kubeconfig
Manage AWS resources
```

---

## Install AWS CLI v2

```bash
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

---

## Verify installation

```bash
aws --version
```

Expected output example:

```text
aws-cli/2.x.x Python/3.x.x Linux/x86_64
```

---

# 5. Configure AWS Credentials

Run:

```bash
aws configure
```

Enter your details:

```text
AWS Access Key ID: your-new-access-key
AWS Secret Access Key: your-new-secret-key
Default region name: eu-central-1
Default output format: json
```

---

## Verify AWS identity

```bash
aws sts get-caller-identity
```

Expected output example:

```json
{
    "UserId": "AIDA...",
    "Account": "123456789012",
    "Arn": "arn:aws:iam::123456789012:user/your-user"
}
```

## What this means

If this command works, your terminal is successfully authenticated with AWS.

> **Important:** Do not create an EKS cluster before `aws sts get-caller-identity` works.

---

# 6. Create EKS Cluster

There are two ways to create the EKS cluster with `eksctl`.

```text
Method A: Direct command
Method B: YAML file approach
```

For learning and real DevOps documentation, the **YAML file approach is better**.

---

# Method A — Create Cluster with Direct Command

This method is simple, but the configuration is only visible in the command.

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

This can take around:

```text
15–25 minutes
```

---

## Explanation of the command

| Option | Meaning |
|---|---|
| `--name` | Name of the EKS cluster |
| `--version` | Kubernetes version |
| `--region` | AWS region |
| `--nodegroup-name` | Name of the worker node group |
| `--node-type` | EC2 instance type for worker nodes |
| `--nodes` | Starting number of worker nodes |
| `--nodes-min` | Minimum number of nodes |
| `--nodes-max` | Maximum number of nodes |
| `--managed` | Creates an AWS-managed node group |

---

# Method B — Create Cluster with YAML File

## Why use YAML?

Using a YAML file is better because:

```text
It is cleaner
It is reusable
It is easier to document
It is easier to update
It can be committed to GitHub
It is closer to real DevOps practice
```

Instead of writing a long command, we describe the cluster in a file.

---

## 1. Create `cluster.yaml`

```bash
nano cluster.yaml
```

---

## 2. Paste this YAML

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

---

## 3. Save the file

Inside `nano`:

```text
CTRL + O
Enter
CTRL + X
```

---

# 7. Understand the `cluster.yaml` File

## Cluster metadata

```yaml
metadata:
  name: jennifer-demo-cluster
  region: eu-central-1
  version: "1.30"
```

| Field | Meaning |
|---|---|
| `name` | Name of the EKS cluster |
| `region` | AWS region where the cluster is created |
| `version` | Kubernetes version |

---

## Managed node group

```yaml
managedNodeGroups:
  - name: jennifer-demo-nodes
    instanceType: t3.medium
    desiredCapacity: 2
    minSize: 1
    maxSize: 3
```

## What this means

| Field | Meaning |
|---|---|
| `managedNodeGroups` | Creates worker nodes managed by AWS |
| `name` | Node group name |
| `instanceType` | EC2 instance type for each worker node |
| `desiredCapacity` | Start with 2 worker nodes |
| `minSize` | Autoscaling should not go below 1 node |
| `maxSize` | Autoscaling can go up to 3 nodes |

---

## Volume size

```yaml
volumeSize: 20
```

This means each worker node gets a 20 GB EBS volume.

---

## Labels

```yaml
labels:
  role: worker
```

Labels are Kubernetes metadata.

They help identify or select nodes later.

---

## Tags

```yaml
tags:
  nodegroup-role: worker
```

Tags are AWS metadata.

They help organize AWS resources.

---

## SSH Access

```yaml
ssh:
  allow: true
```

For learning, this is okay.

In production, many teams avoid SSH and use AWS Systems Manager instead.

---

# 8. Dry Run Before Creating the Cluster

A dry run shows what `eksctl` plans to create without creating real AWS resources.

```bash
eksctl create cluster -f cluster.yaml --dry-run
```

## Why dry run is useful

```text
It checks if your YAML is valid.
It shows the planned cluster configuration.
It helps you catch mistakes before AWS creates resources.
```

---

# 9. Create the Cluster from YAML

If the dry run looks good, create the cluster:

```bash
eksctl create cluster -f cluster.yaml
```

This can take around:

```text
15–25 minutes
```

During this process, `eksctl` creates AWS CloudFormation stacks in the background.

---

# 10. Connect `kubectl` to the EKS Cluster

After the cluster is created, run:

```bash
aws eks update-kubeconfig \
  --region eu-central-1 \
  --name jennifer-demo-cluster
```

## What this command does

It updates your local kubeconfig file so `kubectl` can connect to your EKS cluster.

Your kubeconfig is usually located at:

```text
~/.kube/config
```

---

# 11. Verify the Cluster

## Check nodes

```bash
kubectl get nodes
```

Expected output:

```text
NAME                                           STATUS   ROLES    AGE   VERSION
ip-...eu-central-1.compute.internal            Ready    <none>   ...   ...
```

---

## Check all pods

```bash
kubectl get pods -A
```

This shows pods from all namespaces.

You should see system pods like:

```text
kube-system
coredns
aws-node
kube-proxy
```

---

## Check all services

```bash
kubectl get svc -A
```

---

## Check nodes with more details

```bash
kubectl get nodes -o wide
```

---

## Check current Kubernetes context

```bash
kubectl config current-context
```

---

## List all Kubernetes contexts

```bash
kubectl config get-contexts
```

---

# 12. Useful `eksctl` Commands

## List clusters in a region

```bash
eksctl get cluster --region eu-central-1
```

---

## List node groups

```bash
eksctl get nodegroup \
  --cluster jennifer-demo-cluster \
  --region eu-central-1
```

---

## Scale node group

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

# 13. Delete Everything Later

When you finish the demo, delete the cluster to stop AWS costs.

---

## If you created the cluster with direct command

```bash
eksctl delete cluster \
  --name jennifer-demo-cluster \
  --region eu-central-1
```

---

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

# 14. Clean Local Kubeconfig Context If Needed

Check contexts:

```bash
kubectl config get-contexts
```

Delete the matching context:

```bash
kubectl config delete-context CONTEXT_NAME
```

Example context names may look like:

```text
jules@jennifer-demo-cluster.eu-central-1.eksctl.io
```

or:

```text
arn:aws:eks:eu-central-1:123456789012:cluster/jennifer-demo-cluster
```

---

# 15. Cost Reminder

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

---

## Verify deletion

```bash
eksctl get cluster --region eu-central-1
```

Also check with AWS CLI:

```bash
aws eks list-clusters --region eu-central-1
```

---

# 16. Common Mistakes

## Mistake 1 — Using long dashes

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

---

## Mistake 2 — Using very small worker nodes

Avoid:

```text
t2.micro
```

Use for demo:

```text
t3.medium
```

---

## Mistake 3 — Creating cluster before AWS CLI works

Always test this first:

```bash
aws sts get-caller-identity
```

---

## Mistake 4 — Forgetting to delete the cluster

Always delete when done:

```bash
eksctl delete cluster -f cluster.yaml
```

---

# 17. Recommended Learning Flow

Use this order:

```text
1. Install tools
2. Configure AWS CLI
3. Verify AWS identity
4. Create cluster.yaml
5. Run dry run
6. Create cluster
7. Connect kubectl
8. Verify nodes and pods
9. Continue with Jenkins deployment demo
10. Delete cluster when finished
```

---

## Recommended command flow

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

# 18. Final Rule

For this learning path, prefer the YAML approach:

```bash
eksctl create cluster -f cluster.yaml
```

Because:

```text
It is cleaner.
It is reusable.
It is easier to document.
It is closer to real DevOps practice.
It keeps your cluster configuration visible in Git.
```
