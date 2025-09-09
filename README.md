# Code-Readability-Analysis

This project implements a **readability analysis framework for Java code snippets**, combining **software metrics** with **machine learning classification**. 

---

## 📌 Overview

The tool analyzes Java code snippets (`.jsnp` files) and extracts several well-known software metrics that correlate with readability.  
It then builds a labeled dataset using human readability scores and applies a machine learning classifier to predict readability.

---

## ✨ Features

The following readability metrics are implemented:

1. **Number of Lines (LOC)**  
   Counts the total number of lines in a snippet (including comments and blanks).

2. **Halstead Volume**  
   - Derived from the number of distinct and total operators (`n1`, `N1`) and operands (`n2`, `N2`).  
   - Formula:  
     \[
     V = (N1 + N2) \times \log_2(n1 + n2)
     \]

3. **Cyclomatic Complexity**  
   - Starts at 1, and increments for each control flow element:  
     `if`, `for`, `while`, `do-while`, `foreach`, `switch` cases, `catch` blocks, ternary `?:`, and logical connectors (`&&`, `||`).

4. **Token Entropy**  
   - Uses Shannon entropy over token distributions (including whitespace and special symbols).  
   - Captures predictability vs. diversity of tokens.

---

## ⚙️ How It Works

1. **Preprocessing (Dataset Creation)**  
   - Extracts the selected feature metrics from `.jsnp` files.  
   - Reads a *ground truth CSV* containing human readability ratings.  
   - Converts ratings into binary labels:  
     - Average score ≥ **3.6** → `Y` (Readable)  
     - Average score < **3.6** → `N` (Not Readable)  
   - Saves the output as a **CSV dataset**.

2. **Classification**  
   - Loads the dataset with **Weka**.  
   - Trains a **Logistic Regression** classifier.  
   - Evaluates via **10-fold cross-validation**.  
   - Reports Accuracy, Area Under ROC, and F-Score.

---

## 🚀 Usage

The project is packaged as a runnable JAR with subcommands (using [Picocli](https://picocli.info/)).

### Preprocess – build dataset
```bash
java -jar Readability-Analysis-1.0.jar preprocess \
  --source ./snippets \
  --ground-truth ./truth.csv \
  --target ./dataset.csv \
  lines h_volume token_entropy cyclomatic_complexity
