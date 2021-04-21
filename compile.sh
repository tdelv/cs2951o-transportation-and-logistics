#!/bin/bash

########################################
############# CSCI 2951-O ##############
########################################

# Update this file with instructions on how to compile your code
#javac -cp /gpfs/main/sys/shared/psfu/local/projects/cplex/CPLEX_Studio_Academic/12.3/x86_64/cplex/lib/cplex.jar:/gpfs/main/sys/shared/psfu/local/projects/cplex/CPLEX_Studio_Academic/12.3/x86_64/cpoptimizer/lib/ILOG.CP.jar ./src/solver/ls/*.java
javac -cp ./ILOG.jar ./src/solver/ls/*.java
#javac -cp ./cplex.jar ./src/solver/ls/*.java