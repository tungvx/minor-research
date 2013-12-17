from sklearn import svm
#training data is a list. Each element is also a list of size 2, first is the feature values list, second is the label.
def directLearning(trainingDatas):
    X = []
    Y = []
    for trainingData in trainingDatas:
    	X.append(trainingData[0])
    	Y.append(trainingData[1])
    clf = svm.LinearSVC(loss='l2') # l2 is squared hinge loss
    clf.fit(X, Y)
    return clf.coef_

#TODO   
def aggressiveLearning():
