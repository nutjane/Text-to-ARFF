# Text-to-ARFF
## CONVERTING THE DATASET TO AN ARFF FILE

The Attribute-Relation File Format (ARFF) is an ASCII text file that describes a list of instances sharing a set
of attributes. ARFF files were developed by the Machine Learning Project at the Department of Computer
Science of The University of Waikato for use with the Weka machine learning software.

In document classification, a document is represented with a vector of attribute values where each attribute is a
term, and its value can be either a TFIDF score, frequency in the document, or simply a binary value
indicating whether the document contains such a term or not. 

For example, given your data file as follows:
```
0 Amy likes cat
1 I like dog
0 Dog likes cat
1 Cat likes cat
```
ARFF would represent the above data as:

```
@RELATION SPAM_RSW:true_STEM:true_N:1_W:BINARY
@ATTRIBUTE ami NUMERIC
@ATTRIBUTE cat NUMERIC
@ATTRIBUTE dog NUMERIC
@ATTRIBUTE like NUMERIC
@ATTRIBUTE [class] {0,1}
@DATA
1.0,1.0,0.0,1.0,0
0.0,0.0,1.0,1.0,1
0.0,1.0,1.0,1.0,0
0.0,1.0,0.0,1.0,1
```
The `@RELATION` declaration defines the name of the dataset. The `@RELATION` name should both describe
the dataset and any special configurations used to transform the raw data into the ARFF format. In this
example, `RSW` = whether stop words have been removed, `STEM` = whether a stemming algorithm has been
applied, `N` = N-gram, and `W` = term weighting mode.

The `@ATTRIBUTE` declaration defines each attribute (term) used to represent the document. In this project,
each attribute is a term in the vocabulary extracted from all the documents in the dataset. The last
`@ATTRIBUTE` defines the class attribute. In this case, the class attribute value can be either 0 or 1.

In the example ARFF file above, you can see that there are four data lines, each of which is corresponding to
each document in the original data. Each line has 5 numbers. The first 4 numbers are the terms scores of the
words `ami`, `cat, `dog`, and `like`, respectively. The last number determines the class value of the
document.

This java program is responsible for converting the input data to the ARFF file that Weka can primitively understand. Your task is to implement the following
method:
```java
public void convertTextToArff(String inTextFilename, String outArffFilename,boolean R, boolean S, int N, String W)
```

`inTextFilename` is the filename of the input data file.
`outArffFilename` is the filename of the output ARFF file. Make sure that the output file has the .arff
extension.
Your ARFF converter has to support the following configuration when designing the attribute set and
assigning values to them.

##### Stop Word Removal (R)
If `R = true`, then remove all the stop words from the document before processing. The list of stop words is
defined in stopwords.txt. If `R = false`, do not perform the stop word remov

##### Stemming (S)
If `S = true`, then each word in the document is stemmed before further processing. If `S = false`, do not
perform stemming.

##### N-Gram Generation (N)
`N` represents the upper bound number of N-grams that a document represents. By default, `N = 1`. 

For example, `d = "I like cats a lot"`

```
N = 1 => {i, like, cats, a, lot}
N = 2 => {i, like, cats, a, lot, i-like, like-cats, cats-a, a-lot}
```

##### Term Weighting (W)
Your program should support three term weighting modes: `BINARY`, `NORMFREQ`, and `NORMTFIDF`. 




