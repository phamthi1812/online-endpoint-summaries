# Online Sampling for Computing Summary Powered by RAW-Jena

## Abstract

If you try to collect simple statistics on online SPARQL endpoints,
your SPARQL query is stopped after 60s due to fair usage
policies. This prevents many important downstream tasks, including aggregate
query processing, portal creation, and summarization. Many statistics
can be realized with sampling, as included in the SQL
standard since 2003. However, sampling is still not part of
SPARQL. If integrating sampling in SPARQL seems reasonable, it has
to demonstrate its effectiveness in a real semantic web use-case.
%
In this paper, we evaluate if sampling can be used to create
summaries used in state-of-the-art SPARQL federation
engines. Experimental studies sampling allows to create and maintain
summaries just exploring less than 20\% of datasets.

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
- WARNING: It may converge but will take long time!!!

    ```bash
  java OnlineSummary --dataset pathToTDB2dataset --create_summary pathToNewSummary --GT groundtruth --spo --sampling
    ```
#### For WA without Sampling:

- This is equivalent with executing the query to build the summary => It may takes very long time to finish!

    ```bash
  java OnlineSummary --dataset pathToTDB2dataset --query pathToQuery --create_summary pathToNewSummary --wa 
    ```

#### For WA-Sampling:

- This is the recommended mode. You can modify the ground truth value to decide the size of summary that you want to obtain.

    ```bash
  java OnlineSummary --dataset pathToTDB2dataset --query pathToQuery --create_summary pathToNewSummary --wa --sampling
    ```
  

### It will print out the number of random walks that we need to draw to get the corresponding summary size when you use the sampling flag