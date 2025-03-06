# Artifacts - Integrating Expert Knowledge with Automated Knowledge Component Extraction for Student Modeling

This repository contains the artifacts for the reproducability of our short paper submission, "Integrating Expert Knowledge with Automated Knowledge Component Extraction for Student Modeling" to the UMAP 2025.

The Jupyter notebooks in ast-kc-step-generation/ walk users through generating DataShop-compatible data files from the student submissions using Algorithm 1 we describe in the paper for both AST and Ontology KC models.

The java-parser/ folder includes the files necessary to extract the ontology nodes from the student submissions as a processing step.

The data/ folder contains the processed student submissions for our analysis.

---

The code in this repository extends on the following prior work:

- CodeWorkout dataset provided in CSEDM Data Challenge, organized as part of the 14th International Conference on Educational Data Mining. [https://sites.google.com/ncsu.edu/csedm-dc-2021/dataset](https://sites.google.com/ncsu.edu/csedm-dc-2021/dataset)
- Hosseini, R., & Brusilovsky, P. (2013, January). Javaparser: A fine-grain concept indexing tool for java problems. In CEUR Workshop Proceedings (Vol. 1009, pp. 60-63). University of Pittsburgh. (https://sites.pitt.edu/~paws/ont/java.owl)[https://sites.pitt.edu/~paws/ont/java.owl]
- Demirtas, M. A., Fowler, M., & Cunningham, K. (2024). Reexamining Learning Curve Analysis in Programming Education: The Value of Many Small Problems. In Proceedings of the 17th International Conference on Educational Data Mining (pp. 53-67). (https://github.com/marifdemirtas/ast-kc-step-generation)[https://github.com/marifdemirtas/ast-kc-step-generation]