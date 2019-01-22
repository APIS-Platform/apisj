var CanvasProvider = function CanvasProvider(canvas) {
    this.responseCallbacks = {};
    this.notificationCallbacks = [];
    this.requestId = 0;
    this.canvas = canvas;
};

CanvasProvider.prototype.onMessage = function (data) {
    var _this = this;

    _this._parseResponse(data).forEach(function (result) {

        var id = null;

        // get the id which matches the returned id
        if (Array.isArray(result)) {
            result.forEach(function (load) {
                if (_this.responseCallbacks[load.id]) id = load.id;
            });
        } else {
            id = result.id;
        }

        if(typeof result.result !== undefined && result.result === 'null') {
            result.result = null;
        }


        // notification
        if (!id && result && result.method && result.method.indexOf('_subscription') !== -1) {
            _this.notificationCallbacks.forEach(function (callback) {
                if (_.isFunction(callback)) callback(result);
            });

            // fire the callback
        } else if (_this.responseCallbacks[id]) {
            _this.responseCallbacks[id](null, result);
            delete _this.responseCallbacks[id];
        }
    });
};

/**
 Will parse the response and make an array out of it.

 @method _parseResponse
 @param {String} data
 */
CanvasProvider.prototype._parseResponse = function (data) {
    var _this = this,
        returnValues = [];

    // DE-CHUNKER
    var dechunkedData = data.replace(/\}[\n\r]?\{/g, '}|--|{') // }{
        .replace(/\}\][\n\r]?\[\{/g, '}]|--|[{') // }][{
        .replace(/\}[\n\r]?\[\{/g, '}|--|[{') // }[{
        .replace(/\}\][\n\r]?\{/g, '}]|--|{') // }]{
        .split('|--|');

    dechunkedData.forEach(function (data) {

        // prepend the last chunk
        if (_this.lastChunk) data = _this.lastChunk + data;

        var result = null;

        try {
            result = JSON.parse(data);
        } catch (e) {
            _this.lastChunk = data;

            // start timeout to cancel all requests
            clearTimeout(_this.lastChunkTimeout);
            _this.lastChunkTimeout = setTimeout(function () {
                _this._timeout();
                throw new Error("Returned error : " + data);
            }, 1000 * 15);
            return;
        }

        // cancel timeout and set chunk to null
        clearTimeout(_this.lastChunkTimeout);
        _this.lastChunk = null;

        if (result) returnValues.push(result);
    });

    return returnValues;
};




/**
 Adds a callback to the responseCallbacks object,
 which will be called if a response matching the response Id will arrive.

 @method _addResponseCallback
 */
CanvasProvider.prototype._addResponseCallback = function (payload, callback) {
    var _this = this;
    var id = payload.id || payload[0].id;
    var method = payload.method || payload[0].method;

    _this.responseCallbacks[id] = callback;
    _this.responseCallbacks[id].method = method;

    // schedule triggering the error response if a custom timeout is set
    if (this._customTimeout) {
        setTimeout(function () {
            if (_this.responseCallbacks[id]) {
                _this.responseCallbacks[id](new Error('CONNECTION TIMEOUT: timeout of ' + _this._customTimeout + ' ms achived'));
                delete _this.responseCallbacks[id];
            }
        }, this._customTimeout);
    }
};


/**
 * @param payload payload
 * @param callback triggered on end with (err, result)
 */
CanvasProvider.prototype.send = function (payload, callback) {
    var _this = this;
    _this.requestId += 1;

    var message = JSON.stringify(payload);
    if (!_this.canvas) {
        callback(new Error("Canvas not found"));
        return;
    }

    this.canvas.send(message);
    this._addResponseCallback(payload, callback);
};






/**
 Removes event listener

 @method removeListener
 @param {String} type    'notifcation', 'connect', 'error', 'end' or 'data'
 @param {Function} callback   the callback to call
 */
CanvasProvider.prototype.removeListener = function (type, callback) {
    var _this = this;

    switch (type) {
        case 'data':
            this.notificationCallbacks.forEach(function (cb, index) {
                if (cb === callback) _this.notificationCallbacks.splice(index, 1);
            });
            break;
    }
};

/**
 Removes all event listeners

 @method removeAllListeners
 @param {String} type    'notifcation', 'connect', 'error', 'end' or 'data'
 */
CanvasProvider.prototype.removeAllListeners = function (type) {
    switch (type) {
        case 'data':
            this.notificationCallbacks = [];
            break;
    }
};

/**
 Subscribes to provider events.provider

 @method on
 @param {String} type    'notifcation', 'connect', 'error', 'end' or 'data'
 @param {Function} callback   the callback to call
 */
CanvasProvider.prototype.on = function (type, callback) {

    if (typeof callback !== 'function') throw new Error('The second parameter callback must be a function.');

    switch (type) {
        case 'data':
            this.notificationCallbacks.push(callback);
            break;
    }
};




var global = Function('return this')();
var canvas = global.apisProvider;
var canvasProvider = new CanvasProvider(canvas);

global.apisProvider = canvasProvider;