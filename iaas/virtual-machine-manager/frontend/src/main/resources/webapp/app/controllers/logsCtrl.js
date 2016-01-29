/*
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

(function () {

    angular
        .module('vmmanager.controllers')
        .controller('LogsCtrl', LogsCtrl);

    /* @ngInject */
    function LogsCtrl(LogService) {

        var logsCtrl = this;
        logsCtrl.loading = true;
        logsCtrl.logs = '';
        logsCtrl.logHeaders = [];
        logsCtrl.getDeploymentLogs = getDeploymentLogs;

        activate();

        function activate() {
            loadLogs();
        }

        function loadLogs() {
            LogService
                .getLogs()
                .then(
                    function(response) {
                        logsCtrl.logs = response.data.toString().split('\n');
                        getLogHeaders();
                        toastr.success('Logs loaded.');
                        logsCtrl.loading = false;
                    },
                    function() {
                        toastr.error('Could not load the logs');
                        logsCtrl.loading = false;
                    });
        }

        function getDeploymentLogs(logHeader) {
            var result = [];
            var getLine = false;
            var logId = null;

            logsCtrl.logs.forEach(function(logLine) {
                if (isEndOfEvaluation(logLine) && getLogMessageId(logLine) === logId) {
                    getLine = false;
                }
                if (getLine && getLogMessageId(logLine) === logId) {
                    result.push(logLine);
                }
                if (isStartOfEvaluation(logLine) && getDate(logLine) === getDate(logHeader) &&
                    getTime(logLine) === getTime(logHeader)) {
                    logId = getLogMessageId(logLine);
                    getLine = true;
                }
            });
            return result;
        }

        function getLogHeaders() {
            logsCtrl.logHeaders = [];
            logsCtrl.logs.forEach(function(logLine) {
                if (isStartOfEvaluation(logLine)) {
                    logsCtrl.logHeaders.push(getDate(logLine) + ' ' + getTime(logLine) + ' ' +
                            getSchedAlgorithm(logLine));
                }
            });
        }

        function getLogMessageId(logLine) {
            return logLine.split('--id:')[1];
        }

        function isStartOfEvaluation(logLine) {
            return logLine.split(' ')[6] === '***EVALUATION' && logLine.split(' ')[10] === 'STARTS:';
        }

        function isEndOfEvaluation(logLine) {
            return logLine.split(' ')[6] === '***EVALUATION' && logLine.split(' ')[10] === 'ENDS:';
        }

        function getDate(logLine) {
            return logLine.split(' ')[0];
        }

        function getTime(logLine) {
            return logLine.split(' ')[1];
        }

        function getSchedAlgorithm(logLine) {
            return logLine.split(' ')[11];
        }

    }
    LogsCtrl.$inject = ['LogService'];

})();