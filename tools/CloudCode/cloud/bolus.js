var _ = require('underscore');

var BolusesAfterDateFromTable = function(table, afterDate, limit) {
  var query = new Parse.Query(table);
  console.log("Looking for boluses after " + afterDate);
  query.greaterThan("updatedAt", afterDate);
  query.limit(limit + 0);
  return query.find();
};

var BolusChangeEventsAfter = function(afterDate, limit) {
  var query = new Parse.Query('MedtronicMinimedParadigmRevel755PumpData');
  query.greaterThan('Timestamp', afterDate);
  query.ascending('Timestamp');
  query.limit(limit || 250);
  query.equalTo("Raw_Type", "ChangeCarbRatio");
  query.select("Raw_Type", "Raw_Values", "Timestamp");
  return query.find();
};

var BolusPatternChangeEventsAfter = function (afterDate, limit) {
  var query = new Parse.Query('MedtronicMinimedParadigmRevel755PumpData');
  query.greaterThan('Timestamp', afterDate);
  query.ascending('Timestamp');
  query.limit(limit || 250);
  query.equalTo("Raw_Type", "ChangeCarbRatioPattern");
  query.select("Raw_Type", "Raw_Values", "Timestamp");
  return query.find();
};

var CurrentCarbRatioPatternsAfter = function (afterDate, limit) {
  var query = new Parse.Query('MedtronicMinimedParadigmRevel755PumpData');
  query.greaterThan('Timestamp', afterDate);
  query.ascending('Timestamp');
  query.limit(limit || 250);
  query.equalTo('Raw_Type', 'CurrentCarbRatioPattern');
  query.select('Raw_Type', 'Raw_Values', 'Timestamp');
  return query.find();
};

var CurrentCarbRatiosAfter = function (afterDate, limit) {
  var query = new Parse.Query('MedtronicMinimedParadigmRevel755PumpData');
  query.greaterThan('Timestamp', afterDate);
  query.ascending('Timestamp');
  query.limit(limit || 250);
  query.equalTo('Raw_Type', 'CurrentCarbRatio');
  query.select('Raw_Type', 'Raw_Values', 'Timestamp');
  return query.find();
};

var LogFormatBolusChangeEvent = function(changeEvent) {
  return "Bolus PatternDatumId: " + changeEvent.PatternDatumId +
  " ProfileIndex: " + changeEvent.ProfileIndex +
  " Rate: " + changeEvent.Rate +
  " StartTime: " + changeEvent.StartTime;
};

var LogFormatBolusPatternChangeEvent = function (changeEvent) {
  return "NumRatios: " + changeEvent.NumRatios;
};

var LogFormatCurrentCarbRatioPattern = function (pattern) {
  return "NumRatios: " + pattern.NumRatios;
};

var LogFormatCurrentCarbRatio = function (ratio) {
  return "Bolus PatternDatumId: " + changeEvent.PatternDatumId +
  " ProfileIndex: " + changeEvent.ProfileIndex +
  " Rate: " + changeEvent.Rate +
  " StartTime: " + changeEvent.StartTime;
};

var DeserializeBolusChangeEvent = function(changeEventParseObj) {

  // PATTERN_DATUM_ID=15311235006, INDEX=0, AMOUNT=18, UNITS=grams, START_TIME=0
  var changeEventString = changeEventParseObj.get('Raw_Values');
  var changeEvent = {
    Timestamp: changeEventParseObj.get('Timestamp'),
    PatternDatumId: null,
    ProfileIndex: null, // The first, second, etc rate in the day
    Amount: null, // Carbs per unit of insulin
    StartTime: null // Seconds since midnight
  };
  var elements = changeEventString.split(",");

  _.each(elements, function(element) {
    var pair = element.split("=");
    if (pair.length < 2) {
      return;
    }

    if (/\s*PATTERN_DATUM_ID/.test(pair[0])) {
      changeEvent.PatternDatumId = pair[1];
    }
    else if (/\s*INDEX/.test(pair[0])) {
      changeEvent.ProfileIndex = parseInt(pair[1], 10);
    }
    else if (/\s*AMOUNT/.test(pair[0])) {
      changeEvent.Rate = parseFloat(pair[1], 10);
    }
    else if (/\s*START_TIME/.test(pair[0])) {
      changeEvent.StartTime = parseInt(pair[1], 10);
    }
  });

  return changeEvent;
};

var DeserializeBolusPatternChangeEvent = function(changeEventParseObj) {
  // SIZE=5
  var changeEventString = changeEventParseObj.get('Raw_Values');
  var changeEvent = {
    Timestamp: changeEventParseObj.get('Timestamp'),
    NumRatios: null, // The number of carb ratios in the day
  };
  var elements = changeEventString.split(",");

  _.each(elements, function(element) {
    var pair = element.split("=");
    if (pair.length < 2) {
      return;
    }

    if (/\s*SIZE/.test(pair[0])) {
      changeEvent.NumRatios = parseInt(pair[1], 10);
    }
  });

  return changeEvent;
};

var DeserializeCurrentCarbRatioPattern = function(patternParseObj) {
  // SIZE=5
  var rawValuesString = patternParseObj.get('Raw_Values');
  var patternObj = {
    Timestamp: patternParseObj.get('Timestamp'),
    NumRatios: null, // The number of carb ratios in the day
  };
  var elements = rawValuesString.split(",");

  _.each(elements, function(element) {
    var pair = element.split("=");
    if (pair.length < 2) {
      return;
    }

    if (/\s*SIZE/.test(pair[0])) {
      patternObj.NumRatios = parseInt(pair[1], 10);
    }
  });

  return patternObj;
};

var DeserializeCurrentCarbRatio = function(currentRatioParseObj) {

  // PATTERN_DATUM_ID=15311235006, INDEX=0, AMOUNT=18, UNITS=grams, START_TIME=0
  var rawValuesString = currentRatioParseObj.get('Raw_Values');
  var currentRatio = {
    Timestamp: currentRatioParseObj.get('Timestamp'),
    PatternDatumId: null,
    ProfileIndex: null, // The first, second, etc rate in the day
    Amount: null, // Carbs per unit of insulin
    StartTime: null // Seconds since midnight
  };
  var elements = rawValuesString.split(",");

  _.each(elements, function(element) {
    var pair = element.split("=");
    if (pair.length < 2) {
      return;
    }

    if (/\s*PATTERN_DATUM_ID/.test(pair[0])) {
      currentRatio.PatternDatumId = pair[1];
    }
    else if (/\s*INDEX/.test(pair[0])) {
      currentRatio.ProfileIndex = parseInt(pair[1], 10);
    }
    else if (/\s*AMOUNT/.test(pair[0])) {
      currentRatio.Amount = parseFloat(pair[1], 10);
    }
    else if (/\s*START_TIME/.test(pair[0])) {
      currentRatio.StartTime = parseInt(pair[1], 10);
    }
  });

  return currentRatio;
};

exports.BolusChangeEventsAfter = BolusChangeEventsAfter;
exports.DeserializeBolusChangeEvent = DeserializeBolusChangeEvent;
exports.LogFormatBolusChangeEvent = LogFormatBolusChangeEvent;
exports.BolusPatternChangeEventsAfter = BolusPatternChangeEventsAfter;
exports.DeserializeBolusPatternChangeEvent = DeserializeBolusPatternChangeEvent;
exports.LogFormatBolusPatternChangeEvent = LogFormatBolusPatternChangeEvent;
exports.CurrentCarbRatioPatternsAfter = CurrentCarbRatioPatternsAfter;
exports.LogFormatCurrentCarbRatioPattern = LogFormatCurrentCarbRatioPattern;
exports.DeserializeCurrentCarbRatioPattern = DeserializeCurrentCarbRatioPattern;
exports.CurrentCarbRatiosAfter = CurrentCarbRatiosAfter;
exports.LogFormatCurrentCarbRatio = LogFormatCurrentCarbRatio;
exports.DeserializeCurrentCarbRatio = DeserializeCurrentCarbRatio;

exports.MealsAfterDate = function(afterDate, limit) {
  return BolusesAfterDateFromTable("Meal", afterDate, limit);
};

exports.SnacksAfterDate = function(afterDate, limit) {
  return BolusesAfterDateFromTable("Snack", afterDate, limit);
};

exports.BolusesAfterDate = function(afterDate, limit) {
  var that = this;
  var promise = new Parse.Promise();
  that.MealsAfterDate(afterDate, limit).then(function (meals) {
    that.SnacksAfterDate(afterDate, limit).then(function (snacks) {
      // TODO Sort by date
      promise.resolve(meals.concat(snacks).sort(function(a, b) {
        return a.updatedAt.getTime() - a.updatedAt.getTime();
      }));
    }, function(error) {
      promise.reject(error);
    });
  }, function(error) {
    promise.reject(error);
  });

  return promise;
}
