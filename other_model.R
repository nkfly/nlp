GBM.model = gbm.fit(
  x = train[,1:682] , y = train[,683], 
  distribution = "gaussian",
  n.trees = 100,
  shrinkage = 0.0005,
  interaction.depth = 25,
  n.minobsinnode = 5,
  verbose = TRUE)

pr1 = predict(GBM.model , newdata=test[,1:682] , 100)
table( round(pr1) , test$class)

ada_model = ada(class ~ . , data=train , iter=30 , nu=1 , type="discrete")
summary(ada_model)
pr1 = predict(ada_model , newdata=test)
table( pr1 , test$class)
