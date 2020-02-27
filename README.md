# VCF filter tool
## Running the program
### Options
|Short option|Long option|Description|                   
|---|---|---|
|-f| --filterFile <File>|The filter rules file as described in the next section of this document|   
|-i| --input <File>|Input VCF file|
|-o| --output <File>|The directory to write the ouput to|              
|-q| --route|Generate a 'route' file|
|-r| --replace|Enables output files overwrite|

### Output
The tool produces a number of files:
- The result of the filtering, filename is the input filename postfixed by "filtered"
- The filterfile used, this is copied for reproducability reasons, filename is the input filename postfixed by "filter"
- If the "route" option was provided, a routes file is produced. 
This file contains a line for each variant in the input file, describing the route it followed through the filter tree and what the endresult was.
filename is the input filename postfixed by "filter"

### Example cmd
java -jar FilterTool.jar -i C:\My\Folder\input.vcf -o C:\My\Folder\filter_output -r -q -f C:\My\Folder\filter.txt

## The filter rule file
### Defining steps
To define the tree, simply add a line with "#Steps" as content.
The tool assumes everything below this line to be the tree, or until a "#Tree" line is encountered. 
Lines preceded by a '#', with exception of the "#Tree" are ingnored.

A filter is defined on a single line with the following columns seperated by tabs: 
- name: A name for the step in the tree, this is used to refer to this step from other parts of the config.
- filter: the actual filter, see descriptions in the docs of the filter section of this document.
- type: the type of filter, this can be either "simple", "complex" or "file". If left empty "simple" is assumed.

#### Column types
##### General VCF columns
The general VCF fields can be used in the filters simply by name, for example "#CHROM" or "ALT".
##### INFO
Info fields can be used by surrounding them by "INFO()", for example "INFO("MY_INFO_FIELD_NAME")"
##### VEP
VEP fields can be used by surrounding them by "VEP()", for example "VEP("SYMBOL")"
##### Sample
Sample fields can be used by surrounding them by "SAMPLE()" and specifying the index of the sample, for example "SAMPLE("DP",0)" for the depth of the first sample in the VCF.
Fields shoul be present in the FORMAT column of the variant.

#### Filters
##### Simple
The filter contains the following fields, seperated by spaces:
- name of the column to use
- operator, see section below for the supported operators.
- value that should be used for the operation.

The type column should contain "simple" or be left empty.
###### Supported operators
|Operator|Operation|Example of the filter column|
|---|---|---|
|==|equals|FILTER == PASS|
|\<|less than|VEP(gnomAD_AF) < 0.05|
|\<=|less than or equal|VEP(gnomAD_AF) <= 0.05|
|\>|greater than|VEP(gnomAD_AF) > 0.05|
|\>=|greater or equal|VEP(gnomAD_AF) >= 0.05|
|!=|not equal|SAMPLE(GT) != 0&#124;0|
|in|field is present in the provided list|VEP(SYMBOL) in NMNAT1,PPT1,MUTYH,LMNA|
|contains|specified field contains the specified string|TODO|

##### File
The filter contains the following fields, seperated by spaces:
- name of the column to use
- operator, currently only "in" is supported
- the path to the tsv file followed by a comma and the name of the column in the file that should contain the match.

The type column should contain "file".

###### Supported operators
|Operator|Operation|Example of the filter column|
|---|---|---|
|in|field is present in the provided file|VEP(SYMBOL) in C:\my\dir\myGenePanel.tsv,Gene|
##### Complex
The filter contains the following fields, seperated by spaces:
- comma seperated list of filters
- operator, "and" and "or" are supported.

The type column should contain "complex".
###### Supported operators
|Operator|Operation|Example of the filter column|
|---|---|---|
|AND|All of the provided filters are true|filter1,filter2,filter3 AND|
|OR|Any of the provided filters is true|filter1,filter2,filter3 OR|

### Defining the tree
To define the tree, simply add a line with "#Tree" as content.
The tool assumes everything below this line to be the tree, or until a "#Steps" line is encountered.
Lines preceded by a '#', with exception of the "#Steps" are ingnored.

#### Defining steps
A step is defined on a single line with the following columns seperated by tabs: 
- name: A name for the step in the tree, this is used to refer to this step from other parts of the config.
- filter: the name of the filterstep to be executed
- pass: the name of the next step to be executed when the result of the current filter is "true"
- no-pass: the name of the next step to be executed when the result of the current filter is "false" 
#### Labeling
It is possible to add labels to the "pass" and "no-pass" columns of the previous section by adding them, comma seperated, between braces to those columns.
So for example "step1(label0,label1)".
These labels are added to a INFO field called "FILTER_LABELS" in the vcf.