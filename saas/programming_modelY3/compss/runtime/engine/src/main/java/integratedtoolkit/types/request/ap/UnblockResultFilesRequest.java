package integratedtoolkit.types.request.ap;

import integratedtoolkit.components.impl.AccessProcessor;
import integratedtoolkit.components.impl.DataInfoProvider;
import integratedtoolkit.components.impl.TaskAnalyser;
import integratedtoolkit.components.impl.TaskDispatcher;
import integratedtoolkit.types.data.ResultFile;

import java.util.List;


public class UnblockResultFilesRequest extends APRequest {

    private List<ResultFile> resultFiles;

    public UnblockResultFilesRequest(List<ResultFile> resultFiles) {
        this.resultFiles = resultFiles;
    }

    public List<ResultFile> getResultFiles() {
        return resultFiles;
    }

    public void setResultFiles(List<ResultFile> resultFiles) {
        this.resultFiles = resultFiles;
    }

    @Override
    public void process(AccessProcessor ap, TaskAnalyser ta, DataInfoProvider dip, TaskDispatcher td) {
        for (ResultFile resFile : resultFiles) {
            dip.unblockDataId(resFile.getFileInstanceId().getDataId());
        }
    }

    @Override
    public APRequestType getRequestType() {
        return APRequestType.UNBLOCK_RESULT_FILES;
    }

}
