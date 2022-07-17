package app;

import callback.impl.FileSave2DB;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import task.FileScanner;
import task.FileSearch;
import util.DBInit;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
/**
 * @Author: Waterless
 * @Date: 2022/07/09/15:15
 * @Description: 和界面搭配的类 app.fxml中的所有数据提交给此类来执行后续流程
 *
 */
public class Controller implements Initializable {

    @FXML
    private GridPane rootPane;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<FileMeta> fileTable;

    @FXML
    private Label srcDirectory;

    List<FileMeta> fileMetas;
    //点击运行项目，界面初始化时加载的一个方法
    //就相当于运行一个主类，首先要加载主类的静态块一个道理
 
    private Thread scanThread;
    public void initialize(URL location, ResourceBundle resources) {
        //界面初始化时初始化数据库
        DBInit.init();
        // 添加搜索框监听器，内容改变时执行监听事件
        searchField.textProperty().addListener(new ChangeListener<String>() {

            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                freshTable();
            }
        });
    }
    //点击选择目录，就会获取到最终界面上选择的是哪个文件夹
    public void choose(Event event) {
        // 选择文件目录
        DirectoryChooser directoryChooser=new DirectoryChooser();
        Window window = rootPane.getScene().getWindow();
        File file = directoryChooser.showDialog(window);
        if(file == null)
            return;
        // 获取选择的目录路径，并显示
        String path = file.getPath();
        //在界面中显示路径的内容
        this.srcDirectory.setText(path);
        //获取要扫描的文件夹路径之后，进行文件的扫描工作
//        System.out.println("开始进行文件扫描,根路径为:" + path);
//        long start = System.nanoTime();
        //调用任务类 进行文件扫描任务时 到底信息保存到哪个终端
        //此时保存到数据库
        FileScanner fileScanner = new FileScanner(new FileSave2DB());
        //第一次点击choose 选择目录按钮时 scanThread为空 当扫描到一半 重新选择目录时
        //这个choose方法会重新执行 走到这里 扫描的线程不为空 中断线程
        //然后继续往下走 又会重新选择
        if (scanThread != null) {
            //创建过任务，且该任务没执行结束 中断当前正在扫描的信息
            scanThread.interrupt();
        }
        //开启新线程扫描选择的目录
        scanThread = new Thread( () -> {
            fileScanner.scan(file);
            //  刷新界面 展示刚才扫描到的文件信息
            freshTable();
        });
        scanThread.start();
//        long end = System.nanoTime();
//        System.out.println("共耗时：" + (end - start) *1.0 / 1000000 + "毫秒");
//        System.out.println("文件扫描结束");
//        System.out.println("共扫描到：" + fileScanner.getFileNum() + "个文件");
//        System.out.println("共扫描到：" + fileScanner.getDirNum() + "个文件夹");
        //获取到所有扫描的文件内容
//        this.fileMetas = fileScanner.getFileMetas();
        
        
    }

    // 刷新表格数据
    private void freshTable(){
        //metas表格的内容
        ObservableList<FileMeta> metas = fileTable.getItems();
        metas.clear();
        //用户选择的目录
        String dir = srcDirectory.getText();
        //去除首尾空格
        if (dir != null && dir.trim().length() != 0) {
            // 界面中已经选择了文件，此时已经将最新的数据保存到了数据库中，
            // 只需要取出数据库中的内容展示到界面上即可
            // 获取用户在搜索框中输入的内容
            String content = searchField.getText();
            // 根据选择的路径 + 用户的输入(若为空就展示所有内容) 将数据库中的指定内容刷新到界面中
            List<FileMeta> filesFromDB = FileSearch.search(dir,content);
            metas.addAll(filesFromDB);
        }
        //  扫描文件夹之后 刷新界面
//        if (this.fileMetas != null) {
//            //把所有的文件信息刷新到界面中
//            metas.addAll(fileMetas);
//        }
    }

}
