var basal = require('cloud/basal.js');
var aggregateJobHelper = require('cloud/aggregate_job_helper.js');
var uuid = require('cloud/uuid.js');
var _ = require('underscore');

exports.RegisterAggregateBasalRates = function() {
  console.log("Register aggregate basal rate job");

  var config = aggregateJobHelper.CreateAggregateRateJobConfig();
  config.JobName = 'aggregateBasalRates';
  config.LogTableName = 'AggregateBasalRatesProcessLog';
  config.ChangeEventTableName = 'BasalRate';
  config.PatternChangeEventTableName = 'BasalPattern';
  config.ChangeEventsAfterFun = basal.BasalChangeEventsAfter;
  config.ChangeEventDeserializeFun = basal.DeserializeBasalChangeEvent;
  config.ChangeEventLogFormatFun = basal.LogFormatBasalChangeEvent;
  config.ChangeEventSaveFun = function (parseObj, changeObj) {
    parseObj.set('NLID', uuid.v4());
    parseObj.set('rate', changeObj.Rate);
    parseObj.set('ordinal', changeObj.ProfileIndex);
    parseObj.set('startTime', changeObj.StartTime);
    parseObj.set('timestamp', changeObj.Timestamp);
  };
  config.PatternChangeEventsAfterFun = basal.BasalPatternChangeEventsAfter;
  config.PatternChangeEventDeserializeFun = basal.DeserializeBasalPatternChangeEvent;
  config.PatternChangeEventLogFormatFun = basal.LogFormatBasalPatternChangeEvent;
  config.PatternChangeEventSaveFun = function (parseObj, changeObj) {
    parseObj.set('NLID', uuid.v4());
    parseObj.set('rateCount', changeObj.NumProfiles);
    parseObj.set('timestamp', changeObj.Timestamp);

    var makeResolveAdd = function (po, prms) {
      return function(rateObj) {
        po.add('rates', rateObj);
        if (!!prms.resolvedAt) {
          console.log("resolve " + prms.idd + " " + prms.resolvedAt);
        }
        prms.resolvedAt = new Date();
        prms.resolve();
      };
    };
    var pointerPromises = [];
    for (var i = 0; i < changeObj.NumProfiles; i++) {
      var promise = new Parse.Promise();
      promise.idd = (new Date()).getTime() + _.random(100000);
      pointerPromises.push(promise);
      var query = new Parse.Query(config.ChangeEventTableName);
      query.limit(1);
      query.descending('timestamp');
      query.lessThan('timestamp', changeObj.Timestamp);
      query.equalTo('ordinal', i);
      query.first().then(makeResolveAdd(parseObj, promise), promise.reject);
    }
    var donePromise = new Parse.Promise();
    donePromise.idd = (new Date()).getTime() + _.random(10000);
    var makeResolveDone = function (po, prms) {
      return function() {
        po.save().then(function(){console.log("done " + prms.idd + " " + prms.resolvedAt);prms.resolvedAt = new Date();prms.resolve();}, prms.reject);
      }
    }
    Parse.Promise.when(pointerPromises).then(makeResolveDone(parseObj, donePromise), donePromise.reject);
    return donePromise;
  };

  Parse.Cloud.job('aggregateBasalRates', aggregateJobHelper.CreateAggregateRateJob(config));
};
