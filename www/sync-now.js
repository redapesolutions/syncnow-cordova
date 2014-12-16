var exec = require('cordova/exec');
var pluginName = 'SyncNow';

var SyncNow = function(){
  this.actions = {};
  var sync = this;
  console.debug(sync);
  exec(function(actions) {
    console.debug(actions);
    if (actions) {
      // parse actions into object
      actions.forEach(function(action) {
        sync.actions[action.toLowerCase()] = action;
      });
    }
  }, function(){
    console.error('err');
  }, pluginName, 'INIT', []);
};

SyncNow.prototype.record = function(cb) {
  exec(cb, cb, pluginName, this.actions.record, []);
};

SyncNow.prototype.stopRecording = function(cb) {
  exec(cb, cb, pluginName, this.actions.stop_record, []);
};

module.exports = new SyncNow();
