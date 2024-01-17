package com.microsoft.azure.kusto.data;

import com.microsoft.azure.kusto.data.exceptions.DataWebException;
import com.microsoft.azure.kusto.data.exceptions.KustoServiceQueryError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KustoOperationResultTest {
    @Test
    public void testKustoServiceQueryErrorWithOneApiError() {
        String responseJson = "[{\"FrameType\":\"DataSetHeader\",\"IsProgressive\":false,\"Version\":\"v2.0\",\"IsFragmented\":false,\"ErrorReportingPlacement\":\"EndOfDataSet\"},{\"FrameType\":\"DataTable\",\"TableId\":0,\"TableKind\":\"QueryProperties\",\"TableName\":\"@ExtendedProperties\",\"Columns\":[{\"ColumnName\":\"TableId\",\"ColumnType\":\"int\"},{\"ColumnName\":\"Key\",\"ColumnType\":\"string\"},{\"ColumnName\":\"Value\",\"ColumnType\":\"dynamic\"}],\"Rows\":[[1,\"Visualization\",\"{\\\"Visualization\\\":null,\\\"Title\\\":null,\\\"XColumn\\\":null,\\\"Series\\\":null,\\\"YColumns\\\":null,\\\"AnomalyColumns\\\":null,\\\"XTitle\\\":null,\\\"YTitle\\\":null,\\\"XAxis\\\":null,\\\"YAxis\\\":null,\\\"Legend\\\":null,\\\"YSplit\\\":null,\\\"Accumulate\\\":false,\\\"IsQuerySorted\\\":false,\\\"Kind\\\":null,\\\"Ymin\\\":\\\"NaN\\\",\\\"Ymax\\\":\\\"NaN\\\",\\\"Xmin\\\":null,\\\"Xmax\\\":null}\"]]},{\"FrameType\":\"DataTable\",\"TableId\":1,\"TableKind\":\"PrimaryResult\",\"TableName\":\"PrimaryResult\",\"Columns\":[{\"ColumnName\":\"SourceId\",\"ColumnType\":\"string\"},{\"ColumnName\":\"count_\",\"ColumnType\":\"long\"},{\"ColumnName\":\"EventText\",\"ColumnType\":\"string\"}],\"Rows\":[]},{\"FrameType\":\"DataTable\",\"TableId\":2,\"TableKind\":\"QueryCompletionInformation\",\"TableName\":\"QueryCompletionInformation\",\"Columns\":[{\"ColumnName\":\"Timestamp\",\"ColumnType\":\"datetime\"},{\"ColumnName\":\"ClientRequestId\",\"ColumnType\":\"string\"},{\"ColumnName\":\"ActivityId\",\"ColumnType\":\"guid\"},{\"ColumnName\":\"SubActivityId\",\"ColumnType\":\"guid\"},{\"ColumnName\":\"ParentActivityId\",\"ColumnType\":\"guid\"},{\"ColumnName\":\"Level\",\"ColumnType\":\"int\"},{\"ColumnName\":\"LevelName\",\"ColumnType\":\"string\"},{\"ColumnName\":\"StatusCode\",\"ColumnType\":\"int\"},{\"ColumnName\":\"StatusCodeName\",\"ColumnType\":\"string\"},{\"ColumnName\":\"EventType\",\"ColumnType\":\"int\"},{\"ColumnName\":\"EventTypeName\",\"ColumnType\":\"string\"},{\"ColumnName\":\"Payload\",\"ColumnType\":\"string\"}],\"Rows\":[[\"2024-01-15T19:45:16.8109217Z\",\"KJC.execute;bfdd8642-513b-41f0-99c1-8dbf1a91340e\",\"1ddf9081-c289-42c8-8c3e-323698910b76\",\"f75e856c-65c5-493e-a601-adb37fdf36c5\",\"15a54ff4-611d-48ce-a166-e06425a8898a\",2,\"Error\",-2133196794,\"Query was cancelled (E_QUERY_CANCELLED). (0x80da0006)\",3,\"\",\"{\\\"Count\\\":1,\\\"Text\\\":\\\"Query is expired\\\"}\"],[\"2024-01-15T19:45:16.8109217Z\",\"KJC.execute;bfdd8642-513b-41f0-99c1-8dbf1a91340e\",\"1ddf9081-c289-42c8-8c3e-323698910b76\",\"f75e856c-65c5-493e-a601-adb37fdf36c5\",\"15a54ff4-611d-48ce-a166-e06425a8898a\",4,\"Info\",0,\"S_OK (0)\",5,\"WorkloadGroup\",\"{\\\"Count\\\":1,\\\"Text\\\":\\\"default\\\"}\"],[\"2024-01-15T19:45:16.8109217Z\",\"KJC.execute;bfdd8642-513b-41f0-99c1-8dbf1a91340e\",\"1ddf9081-c289-42c8-8c3e-323698910b76\",\"f75e856c-65c5-493e-a601-adb37fdf36c5\",\"15a54ff4-611d-48ce-a166-e06425a8898a\",4,\"Info\",0,\"S_OK (0)\",6,\"EffectiveRequestOptions\",\"{\\\"Count\\\":1,\\\"Text\\\":\\\"{\\\\\\\"DataScope\\\\\\\":\\\\\\\"default\\\\\\\",\\\\\\\"QueryConsistency\\\\\\\":\\\\\\\"strongconsistency\\\\\\\",\\\\\\\"MaxMemoryConsumptionPerIterator\\\\\\\":5368709120,\\\\\\\"MaxMemoryConsumptionPerQueryPerNode\\\\\\\":8589346816,\\\\\\\"QueryFanoutNodesPercent\\\\\\\":100,\\\\\\\"QueryFanoutThreadsPercent\\\\\\\":100}\\\"}\"],[\"2024-01-15T19:45:16.8109217Z\",\"KJC.execute;bfdd8642-513b-41f0-99c1-8dbf1a91340e\",\"1ddf9081-c289-42c8-8c3e-323698910b76\",\"f75e856c-65c5-493e-a601-adb37fdf36c5\",\"15a54ff4-611d-48ce-a166-e06425a8898a\",6,\"Stats\",0,\"S_OK (0)\",0,\"QueryResourceConsumption\",\"{\\\"ExecutionTime\\\":17.6227443,\\\"resource_usage\\\":{\\\"cache\\\":{\\\"memory\\\":{\\\"hits\\\":0,\\\"misses\\\":0,\\\"total\\\":0},\\\"disk\\\":{\\\"hits\\\":0,\\\"misses\\\":0,\\\"total\\\":0},\\\"shards\\\":{\\\"hot\\\":{\\\"hitbytes\\\":0,\\\"missbytes\\\":0,\\\"retrievebytes\\\":0},\\\"cold\\\":{\\\"hitbytes\\\":0,\\\"missbytes\\\":0,\\\"retrievebytes\\\":0},\\\"bypassbytes\\\":0}},\\\"cpu\\\":{\\\"user\\\":\\\"00:00:00\\\",\\\"kernel\\\":\\\"00:00:00\\\",\\\"total cpu\\\":\\\"00:00:00\\\"},\\\"memory\\\":{\\\"peak_per_node\\\":524384},\\\"network\\\":{\\\"inter_cluster_total_bytes\\\":613,\\\"cross_cluster_total_bytes\\\":0}},\\\"input_dataset_statistics\\\":{\\\"extents\\\":{\\\"total\\\":0,\\\"scanned\\\":0,\\\"scanned_min_datetime\\\":\\\"0001-01-01T00:00:00.0000000Z\\\",\\\"scanned_max_datetime\\\":\\\"0001-01-01T00:00:00.0000000Z\\\"},\\\"rows\\\":{\\\"total\\\":0,\\\"scanned\\\":0},\\\"rowstores\\\":{\\\"scanned_rows\\\":0,\\\"scanned_values_size\\\":0},\\\"shards\\\":{\\\"queries_generic\\\":0,\\\"queries_specialized\\\":0}},\\\"dataset_statistics\\\":[{\\\"table_row_count\\\":0,\\\"table_size\\\":0}],\\\"cross_cluster_resource_usage\\\":{}}\"]]},{\"FrameType\":\"DataSetCompletion\",\"HasErrors\":true,\"Cancelled\":false,\"OneApiErrors\":[{\"error\":{\"code\":\"Internal service error\",\"message\":\"Request aborted due to an internal service error.\",\"@type\":\"Kusto.Data.Exceptions.KustoDataStreamException\",\"@message\":\"Query execution has resulted in error (0x80DA0006): Query is expired.\",\"@context\":{\"timestamp\":\"2024-01-15T19:45:16.8109217Z\",\"serviceAlias\":\"YISCHOEN2\",\"machineName\":\"KEngine000000\",\"processName\":\"Kusto.WinSvc.Svc\",\"processId\":9108,\"threadId\":10152,\"clientRequestId\":\"KJC.execute;bfdd8642-513b-41f0-99c1-8dbf1a91340e\",\"activityId\":\"1ddf9081-c289-42c8-8c3e-323698910b76\",\"subActivityId\":\"1ddf9081-c289-42c8-8c3e-323698910b76\",\"activityType\":\"GW.Http.CallContext\",\"parentActivityId\":\"1ddf9081-c289-42c8-8c3e-323698910b76\",\"activityStack\":\"(Activity stack: CRID=KJC.execute;bfdd8642-513b-41f0-99c1-8dbf1a91340e ARID=1ddf9081-c289-42c8-8c3e-323698910b76 > GW.Http.CallContext/1ddf9081-c289-42c8-8c3e-323698910b76)\"},\"@permanent\":false}}]}]";

        KustoServiceQueryError thrownException = assertThrows(KustoServiceQueryError.class,
                () -> new KustoOperationResult(responseJson, "v2"));

        Assertions.assertTrue(((DataWebException) (thrownException).getExceptions().get(0)).getApiError().getDescription().contains("Query is expired"));
        assertEquals(1, thrownException.getExceptions().size());
        assertSame(thrownException.getExceptions().get(0).getClass(), DataWebException.class);
    }
}
