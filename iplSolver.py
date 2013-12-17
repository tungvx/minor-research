import re
from pulp import *
functions = [["largest", "largest"], ["state", "state"], ["next_to", "borders"], ["const", "Texas"]]
FUNC_ARG = "FUNC_ARG"
def similarity(word1, word2):
    if word1 == word2:
    	return 1;
    return 0;

def features1 (sentence, word1, word2):
    return 0, similarity(word1, word2), 0

def feature2 (sentence, word1, function1, word2, function2):
    similarity = 2
    if (function1[0]  == "largest" and function2[0] == "state"):
    	similarity = 7;
    if (function1[0] == "state" and function2[0] == "next_to"): 
    	similarity = 7;
    if (function1[0] == "next_to" and function2[0] == "const"): 
    	similarity = 7;
    return 0, similarity, 0

def insertToList (composition, wordIndex, functionIndex, wordIndex1, functionIndex1):
    if composition[-1] == [wordIndex, functionIndex]:
	composition.append([wordIndex1, functionIndex1])
	return 1
    elif composition[0] == [wordIndex1, functionIndex1]:
	composition.insert(0, [wordIndex, functionIndex])   
	return 1
    return 0

def insertToResult(finalComposition, wordIndex, functionIndex, wordIndex1, functionIndex1):
    if not finalComposition:
    	finalComposition.append([[wordIndex, functionIndex], [wordIndex1, functionIndex1]])
    else:
	for composition in finalComposition:
	    if insertToList(composition, wordIndex, functionIndex, wordIndex1, functionIndex1):
		return
    	finalComposition.append([[wordIndex, functionIndex], [wordIndex1, functionIndex1]])

def compositeFunctions(finalComposition):
    firstComposition = finalComposition[0] if finalComposition else None
    detected = 0
    for currentComposition in finalComposition[1:]:
	if firstComposition[-1] == currentComposition[0]:
	    firstComposition.pop()
	    firstComposition.extend(currentComposition)
	    finalComposition.remove(currentComposition)
	    detected = 1
	elif firstComposition[0] == currentComposition[-1]:
	    firstComposition.pop(0)
	    firstComposition[:0] = currentComposition;
	    finalComposition.remove(currentComposition)
    	    detected = 1

    if detected and len(finalComposition) > 1:
    	compositeFunctions(finalComposition)

def iplSolver (sentence, w):
    words = re.compile("([\w][\w']*\w)").findall(sentence)
    wordLength = len(words)
    functionLength = len(functions)

    alpha = [[] for i in range(wordLength)]
    beta = [[[[] for i in range(wordLength)] for i in range(functionLength)] for i in range(wordLength)]

    # the problem:
    prob = LpProblem("myProblem", LpMaximize)

    obj = 0
    for wordIndex in range(wordLength):
	contraints = 0
	for functionIndex in range(functionLength):
	    alpha[wordIndex].append(LpVariable(str(wordIndex) + "-" + str(functionIndex), 0, 1, LpBinary))
	    contraints += alpha[wordIndex][functionIndex]
	    featureValue = lpDot(w, features1(sentence, words[wordIndex], functions[functionIndex][1]))
	    if featureValue:
	    	obj += alpha[wordIndex][functionIndex] * featureValue 
	    else:
	     	prob += alpha[wordIndex][functionIndex] == 0
    	prob += contraints <= 1;

    for wordIndex in range(wordLength):
	for functionIndex in range(functionLength):
    	    # contraints for beta: among beta_cs,dtS, there is only one beta_cs,dt
	    compositionContraints = 0
    	    for wordIndex1 in range(wordLength):
		for functionIndex1 in range(functionLength):
		    beta[wordIndex][functionIndex][wordIndex1].append(LpVariable(str(wordIndex) + "-" + str(functionIndex)  + "-" + str(wordIndex1)  + "-" + str(functionIndex1), 0, 1, LpBinary))
		    compositionContraints += beta[wordIndex][functionIndex][wordIndex1][functionIndex1]		    
		    # Beta_cs,dt is active, s must be a function and types of s and t should be consistent
		    # This case should be implemented by feature calculation. When types of functions are not matched, 
		    # the feature returned must be 0
		    featureValue = lpDot(w, feature2(sentence, words[wordIndex], functions[functionIndex], words[wordIndex1], functions[functionIndex1]))
    		    if featureValue:
		    	obj += beta[wordIndex][functionIndex][wordIndex1][functionIndex1] * featureValue
		    else:
	    		prob += beta[wordIndex][functionIndex][wordIndex1][functionIndex1] == 0
		    # Beta_cs,dt is active if and only if alpha_cs and alpha_dt are active
		    prob += 2 * beta[wordIndex][functionIndex][wordIndex1][functionIndex1] <= alpha[wordIndex][functionIndex] + alpha[wordIndex1][functionIndex1]
		    
		    # Functional composition is directional and acyclic
		    # ........First: directional (acyclic is solved later):.................
	    prob += compositionContraints <= 1

    # functional composition is acyclic		    
    for wordIndex in range(wordLength):
	for functionIndex in range(functionLength):
	    compositionContraints = 0;
    	    for wordIndex1 in range(wordLength):
		for functionIndex1 in range(functionLength):
		    compositionContraints += beta[wordIndex1][functionIndex1][wordIndex][functionIndex]
		    prob += beta[wordIndex][functionIndex][wordIndex1][functionIndex1] + beta[wordIndex1][functionIndex1][wordIndex][functionIndex] <=  1
	    prob += compositionContraints <= 1	     

    prob += obj
    prob.solve()

    # output the return
    finalComposition = []
    resultIndex = 0;
    for wordIndex in range(wordLength):
	for functionIndex in range(functionLength):
	    for wordIndex1 in range(wordLength):
		for functionIndex1 in range(functionLength):
		    if (value(beta[wordIndex][functionIndex][wordIndex1][functionIndex1])):
			print words[wordIndex] + ":" + functions[functionIndex][0] + " - " + words[wordIndex1] + ":" + functions[functionIndex1][0]
			insertToResult(finalComposition, wordIndex, functionIndex, wordIndex1, functionIndex1)

    result = FUNC_ARG
    compositeFunctions(finalComposition)
    for function in finalComposition[0]:
    	result = result.replace(FUNC_ARG, functions[function[1]][0] + "(" + FUNC_ARG + ")")
	if functions[function[1]][0] == "const":
    	    result = result.replace(FUNC_ARG, words[function[0]].lower())
    return result

