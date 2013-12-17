from iplSolver import *
from executeDatabase import *
from trainingData import *
w = [5, 5, 5]

def feedback (query, expectedResult):
    return 1 if expectedResult == execute(query) else -1

def main():
    for trainingData in readTrainingData():
   	result = iplSolver(trainingData[0], w)
	print result
	f = feedback(result, trainingData[1])
main()
