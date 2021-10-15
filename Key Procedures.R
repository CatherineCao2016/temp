# @hidden_cell
library(ibmWatsonStudioLib)
wslib <- access_project_or_space()

Db2_WoC_metadata = wslib$get_connection("Db2 WoC")

library(RJDBC)

drv <- JDBC(driverClass="com.ibm.db2.jcc.DB2Driver", classPath="/opt/ibm/connectors/db2/db2jcc4.jar")

Db2_WoC_url <- paste("jdbc:db2://",
                     Db2_WoC_metadata[][["host"]],
                     ":", Db2_WoC_metadata[][["port"]],
                     "/", Db2_WoC_metadata[][["database"]],
                     ":", "sslConnection=true;",
                     sep=""
)

Db2_WoC_connection <- dbConnect(drv,
                                Db2_WoC_url,
                                Db2_WoC_metadata[][["username"]],
                                Db2_WoC_metadata[][["password"]]
)

# NOTE:
#  A row limit has been applied to the query to enable sample previewing.
#  Adjust the display message and query as needed by editing the following lines:
library(IRdisplay)
display_html("A row limit of 5000 has been applied to the query to enable sample previewing. If the data set is larger, only the first 5000 rows will be loaded.")
query <- "SELECT * FROM \"HMCRD\".\"APPLICATION_TRAIN\" FETCH FIRST 10000 ROWS ONLY"

data <- dbSendQuery(Db2_WoC_connection, query)
# fetch first 5 rows
Train <- dbFetch(data, n = 10000)

# convert data types
data_type <- data.frame(dbColumnInfo(data))
df1 <- as.data.frame(lapply(Train[levels(factor(data_type$field.name[data_type$field.type == "DECFLOAT"]))], as.numeric))
df2 <- Train[setdiff(data_type$field.name, data_type$field.name[data_type$field.type == "DECFLOAT"])]
dt1 <- cbind(df1, df2)

# Box-Cox transform
preProcValues <- preProcess(dt1, method = "BoxCox")
dt1_tran <- predict(preProcValues, dt1)

#Recreate numeric list with new dt1_tran
numeric_list <- unlist(lapply(dt1_tran, is.numeric))
dt1_num <- setDT(dt1_tran)[,..numeric_list]

# Missing values => Numeric
mv <- as.data.frame(apply(dt1_tran, 2, function(col)sum(is.na(col))/length(col)))
colnames(mv)[1] <- "missing_values"
mv <- index_to_col(mv,'Column')
mv <- setDT(mv)[order (missing_values, decreasing = TRUE)]

ggplot (mv[1:40,], aes (reorder(Column, missing_values), missing_values)) + geom_bar (position = position_dodge(), stat = "identity") + coord_flip () + xlab('Columns') + ylab('Missing Value %')

dt1_num2 <- na.aggregate(dt1_num)

# Non-numeric
non_numeric_list <- unlist(lapply(dt1_tran, is.character))
dt1_non_num <- setDT(dt1_tran)[,..non_numeric_list]

# create dummies for non-numeric cols
dt1_non_num_dum <- dummy_cols(dt1_non_num, select_columns = colnames(dt1_non_num))

# Attaching numeric and non numeric columns and handling missing values
dt1_preproc <- cbind(dt1_non_num_dum,dt1_num)

mv <- as.data.frame(apply(dt1_preproc, 2, function(col)sum(is.na(col))/length(col)))
colnames(mv)[1] <- "missing_values"
mv <- index_to_col(mv,'Column')
mv <- setDT(mv)[order (missing_values, decreasing = TRUE)]

ggplot (mv[1:40,], aes (reorder(Column, missing_values), missing_values)) + geom_bar (position = position_dodge(), stat = "identity") + coord_flip () + xlab('Columns') + ylab('Missing Value %')

dt1_preproc <- na.aggregate(dt1_preproc)

# split into training and test
set.seed(1234)
dt1_preproc_sample <- dt1_preproc

cols_to_keep <- c('FLAG_OWN_CARN','`ORGANIZATION_TYPEIndustry: type 1`','DAYS_ID_PUBLISH','SK_ID_CURR','REG_CITY_NOT_LIVE_CITY','YEARS_BEGINEXPLUATATION_MODE','COMMONAREA_MODE','FLOORSMAX_MODE','LIVINGAPARTMENTS_MODE','YEARS_BUILD_MEDI','CODE_GENDERM','OCCUPATION_TYPEWaiters/barmen staff','TARGET','EXT_SOURCE_1','EXT_SOURCE_2','EXT_SOURCE_3')
dt1_preproc_sample <- as.data.frame(dt1_preproc_sample)[, (colnames(dt1_preproc_sample) %in% cols_to_keep)]

predictors <- setDT(dt1_preproc_sample)[,-c('TARGET')]
classes <- as.factor(dt1_preproc_sample$TARGET)
trainingRows <- createDataPartition(y=classes, p = 0.80, list =FALSE)
trainPredictors <- predictors[trainingRows,]
trainclasses <- classes[trainingRows]
testPredictors <- predictors[-trainingRows,]
testClasses <- classes[-trainingRows]

dt1_preproc_sample <- mutate(dt1_preproc_sample, TARGET = ifelse(TARGET == 0,'Yes',"No"))
dt1_preproc_sample$TARGET <- as.factor(dt1_preproc_sample$TARGET)

inTrain <- createDataPartition(dt1_preproc_sample$TARGET, p = .8)[[1]]
dtTrain <- dt1_preproc_sample[ inTrain, ]
dtTest  <- dt1_preproc_sample[-inTrain, ]

traincntrl <- trainControl(method = 'repeatedcv',
                           number = 5,
                           repeats = 2,
                           classProbs = TRUE, 
                           sampling = "down",
                           summaryFunction = twoClassSummary)

trainPredictors <- as.matrix(trainPredictors)

svmFitLinear <- train(TARGET ~.,
                      data = dtTrain,
                      method = 'svmLinear',
                      preProc = c('center','scale'),
                      metric = "ROC",
                      tuneLength = 7,
                      trControl = traincntrl)


dtTest$svmFitLinearclass <- predict(svmFitLinear, dtTest)
dtTest$svmFitLinearprobs_yes <- predict(svmFitLinear, newdata = dtTest , type = "prob")[[2]]


confusionMatrix(data = dtTest$svmFitLinearclass,
                reference = dtTest$TARGET,
                positive = "Yes")



