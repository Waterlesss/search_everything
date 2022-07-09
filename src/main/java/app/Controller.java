package app;

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
import util.DBInit;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
/**
 * @Author: Waterless
 * @Date: 2022/07/09/15:15
 * @Description:
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
        System.out.println("开始进行文件扫描,根路径为:" + path);
        long start = System.nanoTime();
        FileScanner fileScanner = new FileScanner();
        fileScanner.scan(file);
        long end = System.nanoTime();
        System.out.println("共耗时：" + (end - start) *1.0 / 1000000 + "毫秒");
        System.out.println("文件扫描结束");
        System.out.println("共扫描到：" + fileScanner.getFileNum() + "个文件");
        System.out.println("共扫描到：" + fileScanner.getDirNum() + "个文件夹");
        //获取到所有扫描的文件内容
        this.fileMetas = fileScanner.getFileMetas();
        // TODO 刷新界面 展示刚才扫描到的文件信息
        freshTable();
    }

    // 刷新表格数据
    private void freshTable(){
        //metas表格的内容
        ObservableList<FileMeta> metas = fileTable.getItems();
        metas.clear();
        // TODO 扫描文件夹之后 刷新界面
        if (this.fileMetas != null) {
            //把所有的文件信息刷新到界面中
            metas.addAll(fileMetas);
        }
    }

}
