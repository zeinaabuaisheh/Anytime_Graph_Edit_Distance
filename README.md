Anytime Graph Edit Distance Approaches for Pattern Recognition

## Introduction

This repository corresponds to the approaches that have been put forward to solve the problem of Graph Edit Distance and were implemented during the PhD of Zeina Abu-Aisheh at LIFAT laboratory:

- The first method's name is Depth-First (DF) and was published in the following conference:
[Zeina Abu-Aisheh, Romain Raveaux and Jean-Yves Ramel. "Graph Edit Distance for Solving Pattern Recognition Problems." *ICPRAM.* 2015](http://www.rfai.li.univ-tours.fr/PagesPerso/zabuaisheh/documents/icpram.pdf)


Please cite:
 
```
@inproceedings{DBLP:conf/icpram/Abu-AishehRRM15,
  author    = {Zeina Abu{-}Aisheh and
               Romain Raveaux and
               Jean{-}Yves Ramel and
               Patrick Martineau},
  title     = {An Exact Graph Edit Distance Algorithm for Solving Pattern Recognition
               Problems},
  booktitle = {{ICPRAM} 2015 - Proceedings of the International Conference on Pattern
               Recognition Applications and Methods, Volume 1, Lisbon, Portugal,
               10-12 January, 2015.},
  pages     = {271--278},
  year      = {2015},
  crossref  = {DBLP:conf/icpram/2015-1},
  timestamp = {Tue, 15 Sep 2015 17:18:51 +0200},
  biburl    = {https://dblp.org/rec/bib/conf/icpram/Abu-AishehRRM15},
  bibsource = {dblp computer science bibliography, https://dblp.org}
}
```

- The second method's name is Parallel Depth-First (PDFS) and was published in the following journal:

[Zeina Abu-Aisheh, Romain Raveaux, Jean-Yves Ramel and Patrick Martineau. "A parallel graph edit distance algorithm." * Expert Syst.* 2018.](http://www.rfai.li.univ-tours.fr/PagesPerso/zabuaisheh/documents/ESW2018-Zeina.pdf)

Please cite: 

```
@article{DBLP:journals/eswa/Abu-AishehRRM18,
  author    = {Zeina Abu{-}Aisheh and
               Romain Raveaux and
               Jean{-}Yves Ramel and
               Patrick Martineau},
  title     = {A parallel graph edit distance algorithm},
  journal   = {Expert Syst. Appl.},
  volume    = {94},
  pages     = {41--57},
  year      = {2018},
  url       = {https://doi.org/10.1016/j.eswa.2017.10.043},
  doi       = {10.1016/j.eswa.2017.10.043},
  timestamp = {Tue, 28 Nov 2017 16:10:54 +0100},
  biburl    = {https://dblp.org/rec/bib/journals/eswa/Abu-AishehRRM18},
  bibsource = {dblp computer science bibliography, https://dblp.org}
}
```

- These methods can be transformed into anytime approaches thanks to the time and memory constraints where the user can output an answer at anytime.

The anytime version of DF and PDF are referred to as ADF and APDF, respectively. Anytime Graph Matching was published in the following journal:

[Zeina Abu-Aisheh, Romain Raveaux and Jean-Yves Ramel. "Anytime graph matching." * Pattern Recognition Letters.* 2016.](https://www.sciencedirect.com/science/article/abs/pii/S0167865516302690)


Please cite: 
```
@article{DBLP:journals/prl/Abu-AishehRR16,
  author    = {Zeina Abu{-}Aisheh and
               Romain Raveaux and
               Jean{-}Yves Ramel},
  title     = {Anytime graph matching},
  journal   = {Pattern Recognition Letters},
  volume    = {84},
  pages     = {215--224},
  year      = {2016},
  url       = {https://doi.org/10.1016/j.patrec.2016.10.004},
  doi       = {10.1016/j.patrec.2016.10.004},
  timestamp = {Fri, 30 Nov 2018 13:29:16 +0100},
  biburl    = {https://dblp.org/rec/bib/journals/prl/Abu-AishehRR16},
  bibsource = {dblp computer science bibliography, https://dblp.org}
}
```




## Datasets

The cost functions of three datasets (CMU, GREC and Mutagenicity) are included. For each data set, specific cost functions were used.
To take a look at their definitions, please check ``` src/util/CMUCostFunction.java ```, ``` src/util/GRECCostFunction.java ``` and ``` src/util/MutagenCostFunction.java ```
To know more about each of the datasets, we refer the reader to [the GDR4GED repository](http://www.rfai.li.univ-tours.fr/PublicData/GDR4GED/home.html) and the following article:

[Zeina Abu-Aisheh, Romain Raveaux and Jean-Yves Ramel. "A Graph Database Repository and Performance Evaluation Metrics for Graph Edit Distance." *GBR.* 2015.](https://link.springer.com/chapter/10.1007/978-3-319-18224-7_14)



## Run the Code

To run the codes, you can generate the jar files of ADF and APDFS, separately, then open the terminal and type:


``` java -jar *****.jar [TIME_CONSTRAINT_IN_MILLISECONDS] [THE_PATH_OF_THE_SOURCE_GRAPH] [The_PATH_OF_THE_SECOND_GRAPH] [THE_NUMBER_OF_THE_DATASET] ```


  
Remark : heap and stack memory size can be provided, as follows:

 ``` java -Xss20m -Xmx10200m -jar *****.jar [TIME_CONSTRAINT_IN_MILLISECONDS] [THE_PATH_OF_THE_SOURCE_GRAPH] [The_PATH_OF_THE_SECOND_GRAPH] [THE_NUMBER_OF_THE_DATASET] ```


### Dataset Choice

Note that datasets are given numbers in the code: The number 1 refers to GREC dataset, number 2 to Mutagenicity dataset, and number 3 to Protein dataset.
If the user chooses Mutagenicity (i.e., 2), four other parameters have to be choosen too (substituiton, deletion and insertion of edges and vertices), insertion and deletions of vertices are similar and same applies to edges' deletion and insertion, thus to run the code on Mutagenicity:

 ``` java -Xss20m -Xmx10200m -jar *****.jar [TIME_CONSTRAINT_IN_MILLISECONDS] [THE_PATH_OF_THE_SOURCE_GRAPH] [The_PATH_OF_THE_SECOND_GRAPH] [THE_NUMBER_OF_THE_DATASET] [SUB_VERTICES_COST] [INSERT_DELETE_VERTICES_COST]  [SUB_EDGES_COST] [INSERT_DELETE_EDGES_COST]```
 

