data = read.csv("doucment_vector.csv")
set.seed(144) ### load caTools library
spl = sample.split(data$class , SplitRatio=0.8)

#data$class = as.numeric(data$class)

train = subset(data , spl==TRUE)
test = subset(data , spl==FALSE)

lm_model = lm( class ~ . , data=train)
rpart_model = rpart( as.factor(class) ~ . , data=train )
prp(rpart_model)
rf_model = randomForest( as.factor(class) ~ . -X5 , data=train , ntree=500 , mtry=57 , importance=TRUE)
pred_test = predict(rf_model , newdata=test , type="class")
pred_test_rpart = predict(rpart_model , newdata=test , type="class")
table(pred_test, test$class)

### load ROCR package
ROCRpred = prediction(pred_test[,2], test$class)
ROCRperf = performance(ROCRpred, "tpr", "fpr")
plot(ROCRperf, colorize=TRUE)
auc = as.numeric(performance(ROCRpred, "auc")@y.values)

tuneRF(train[,1:682] , train[,683] , improve=0.005 , dobest=TRUE)
