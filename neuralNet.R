install.packages('neuralnet')
library("neuralnet")
data = read.csv("doucment_vector.csv", header=TRUE)

xnam <- paste("X", 1:(length(data)-1), sep="")
fmla <- as.formula(paste("class ~ ", paste(xnam, collapse= "+")))

net <- neuralnet(fmla, data, hidden=10, threshold=0.01)
