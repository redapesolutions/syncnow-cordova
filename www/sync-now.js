var exec = require('cordova/exec');
var pluginName = 'SyncNow';

var SyncNow = function(){
  var sync = this;
  exec(function(actions) {
    if (actions) {
      // parse actions into object
      actions.forEach(function(action) {
        sync.actions[action.toLowerCase()] = action;
      });
    }
  }, console.error, pluginName, 'INIT', []);
};

SyncNow.prototype.record = function(cb) {
  exec(cb, cb, pluginName, this.actions.record, []);
};

module.exports = new SyncNow();
