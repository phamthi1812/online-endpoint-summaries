# Why is Sampling Essential in SPARQL? Exploring the Case of Online Endpoint Summaries

## Abstract

Collecting simple statistics from online public SPARQL
endpoints is hampered by their fair usage policies. This restriction
obstructs several critical operations, such as aggregate query
processing, portal development, and data summarization. Online sampling allows
to collect statistics while respecting fair usage policies. However, sampling has not yet been  integrated into the
SPARQL standard. Although integrating sampling into the SPARQL
standard appears beneficial, its effectiveness must be proven in a
practical semantic web context. This poster investigates whether online sampling can
generate summaries for use in cutting-edge SPARQL federation
engines. Our experimental studies indicate that sampling enables the
creation and maintenance of summaries by exploring less than 20\% of datasets.

**Keywords**: Semantic Web ,SPARQL , Sampling, Summary

## Installation

> **Note**
> Installation instructions have only been tested on Ubuntu 20.04.6 LTS

### Dependencies

- conda
- maven
- java 11 & 20 (JDK)

### Step by step installation

- Load dataset into Apache Jena by TDB2 XLoader

- Install project dependencies

    > **Warning**
    > The project uses Maven Toolchains. Make sure that the location of `java 11` and `java 20` is defined in `~/.m2/toolchains.xml`. 
  
    ```bash
    mvn clean install
    ```

### Run Experiments

#### For SPO-Sampling:

- Ground Truth Summary Size for FedShop200 is 5800 and LargeRDFBench is 6070
- WARNING: It may converge but will take very long time!!!

    ```bash
  java OnlineSummary --dataset pathToTDB2dataset --create_summary pathToNewSummary --GT groundtruth --spo --sampling
    ```
#### For WA without Sampling:

- This is equivalent with executing the query to build the summary => It may takes very long time to finish TOO!

    ```bash
  java OnlineSummary --dataset pathToTDB2dataset --query pathToQuery --create_summary pathToNewSummary --wa 
    ```

#### For WA-Sampling:

- This is the recommended mode. You can modify the ground truth value to decide the size of summary that you want to obtain.

    ```bash
  java OnlineSummary --dataset pathToTDB2dataset --query pathToQuery --create_summary pathToNewSummary --wa --sampling
    ```
  

### Number of random walks that we need to draw and the corresponding summary size will be printed out when you use the sampling flag