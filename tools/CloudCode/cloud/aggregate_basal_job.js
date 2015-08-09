var _ = require('underscore');
var basal = require('cloud/basal.js');


exports.RegisterAggregateBolusRates = function() {
  Parse.Cloud.job('aggregateBolusRates', function(request, status) {
    // Get the last time this ran
    var lastProcessedDate = null;
    var processLogTableName = 'AggregateBolusRatesProcessLog';

    var query = new Parse.Query(processLogTableName);
    query.descending('createdAt');
    query.first({
      success: function (object) {
        if (!!object) {
          // Last time
          console.log("Last log " + object.id);
          lastProcessedDate = object.get('lastProcessedDate');
        }
        else {
          lastProcessedDate = new Date(0);
        }

        console.log("Last processed date" + lastProcessedDate);
        var changeEventsPromise = BasalChangeEventsAfter(lastProcessedDate, 250);
        lastProcessedDate = null;

        changeEventsPromise.then(function(changeEvents) {
          var resolveCount = changeEvents.length;
          console.log(resolveCount + " basal change events");
          if (resolveCount === 0) {
            status.success("No basal changes to process");
            return;
          }

          var rates = [];
          var tryResolve = function(changeEvent) {
            if (changeEvent &&
              (!!!lastProcessedDate ||
                changeEvent.updatedAt.getTime() < lastProcessedDate.getTime())) {
              lastProcessedDate = changeEvent.updatedAt;
            }

            if (--resolveCount === 0) {
              var rateResolveCount = rates.length;
              _.each(rates, function(rate) {
                // TODO Replace oridnal
                var rateItem = new Parse.Object('BasalRate');
                rateItem.put('rate', rate.Rate);
                rateItem.put('oridnal', rate.ProfileIndex);
                rateItem.put('startTime', rate.StartTime);

                rateItem.save(null, {
                  success: funtion() {},
                  error: function(error) {}
                });
              });

              var logItem = new Parse.Object(processLogTableName);
              logItem.set("lastProcessedDate", new Date(lastProcessedDate));
              logItem.save(null, {
                success: function() {
                  status.success("Basal aggregation success");
                },
                error: function(saveError) {
                  status.error("Basal aggregation log save error " + saveError.errors + " "+ saveError.code + " " + saveError.message);
                }
              });
            }
          };

          _.each(changeEvents, function(changeEvent) {
            var changeObj = DeserializeBasalChangeEvent(changeEvent.get("Raw-Values"));
            rates[changeObj.ProfileIndex] = changeObj;

            tryResolve(changeEvent);
          });
        }, function(error) {
          status.error("Basal aggregation error " + error.message);
        });
      },
      error: function (error) {
        status.success("Basal aggregation error " + error.message);
      }
    });
  });
};
