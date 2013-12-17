# calculate the similarity between two words
# http://nltk.googlecode.com/svn/trunk/doc/howto/wordnet.html
# Leacock-Chodorow Similarity
from nltk.corpus import wordnet

from nltk.corpus import wordnet as wn

def similarity(x, y):
    word1 = wn.synset(x + '.n.01')
    word2 = wn.synset(y + '.n.01')
    
    return word1.lch_similarity(word2)
