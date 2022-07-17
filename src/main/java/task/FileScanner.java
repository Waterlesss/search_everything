package task;

import app.FileMeta;
import callback.FileScanCallBack;
import lombok.Getter;

import java.io.File;
import java.rmi.activation.ActivationMonitor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Waterless
 * @Date: 2022/07/09/15:25
 * @Description: 进行文件的扫描任务类
 * 递归的访问磁盘中指定文件夹的所有文件
 */

@Getter
public class FileScanner {
    //当前扫描的文件个数
    private AtomicInteger fileNum = new AtomicInteger();
    //当前扫描的文件夹个数
    //最开始扫描的根路径没有统计，因此初始化文件夹的个数为1，表示从根目录下开始进行扫描任务
    private AtomicInteger dirNum = new AtomicInteger(1);
    
    //所有扫描文件的子线程个数，只有当子线程个数为0的时候，主线程再继续执行
    private AtomicInteger threadCount = new AtomicInteger();
    
    //当最后一个子线程执行完任务之后，再调用countDown方法唤醒主线程
    //实现最大的并行性：有时我们想同时启动多个线程，实现最大程度的并行性。
    // 例如，我们想测试一个单例类。如果我们创建一个初始计数器为1的CountDownLatch，
    // 并让其他所有线程都在这个锁上等待，
    // 只需要调用一次countDown()方法就可以让其他所有等待的线程同时恢复执行。
    private CountDownLatch latch = new CountDownLatch(1);
    
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    
    //使用线程池创建对象
    private ThreadPoolExecutor pool = new ThreadPoolExecutor(CPU_COUNT,CPU_COUNT * 2,
            10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),//无界阻塞队列
            new ThreadPoolExecutor.AbortPolicy() //拒绝策略 超出最大线程数的直接拒绝
            );
    
    
    //扫描的所有文件信息
//    List<FileMeta> fileMetas = new ArrayList<>();
    //文件扫描回调对象 接收一个接口对象，啥时候传进来 由构造方法传
    private FileScanCallBack callBack;

    public FileScanner(FileScanCallBack callBack) {
        this.callBack = callBack;
    }
    /**
     * 根据传入的文件夹进行扫描任务
     * @param filePath 要扫描的根目录
     *选择要扫描的菜单之后，执行的第一个方法，
     *主线程需要等待所有的子线程全部扫描结束之后再恢复执行
     *scan方法是我们选择要扫描的文件夹之后的入口方法，所有文件夹和文件的具体扫描工作交给子线程
     *scan方法等待所有子线程执行结束之后，统计扫描到的文件夹和文件个数，计算扫描时间                 
     */
    public void scan (File filePath) {
        System.out.println("开始文件扫描任务,根目录为: " + filePath);
        long start = System.nanoTime();
        //将具体的扫描任务交给子线程处理
        scanInternal(filePath);
        //提交的时候已经有一个线程了（根目录）
        threadCount.incrementAndGet();
        try {
            latch.await();
        } catch (InterruptedException e) {
            System.err.println("扫描任务中断，根目录为：" + filePath);
        } finally {
            System.out.println("关闭线程池");
            //所有子线程执行结束，就是正常关闭
            //中断任务，需要立即停止所有还在扫描的任务
            pool.shutdownNow();
        }
        //latch恢复执行以后 此时文件扫描任务已经结束了
        long end = System.nanoTime();
        System.out.println("文件扫描结束，共耗时：" + (end - start) * 1.0 / 1000000 + "ms");
        System.out.println("文件扫描结束 根目录为：" + filePath);
        System.out.println("共扫描到：" + dirNum + "个文件夹 " + fileNum + "个文件");
        
//        //终止条件
//        if (filePath == null) {
//            return;
//        }
//        //使用回调函数，将当前目录下的所有内容保存到指定终端
//        //callback就是将扫描到的文件夹信息保存到终端中(此时是数据库)
//        //将当前路径下的所有文件信息保存到数据库中
//        this.callBack.callback(filePath);
//        //先将当前目录下的file对象获取出来
//        File[] files = filePath.listFiles();
//        //遍历这些file对象，根据是否是文件夹 进行区别处理
//        for (File file : files) {
////            FileMeta meta = new FileMeta();
//            if (file.isDirectory()) {
//                //是文件夹继续递归扫描
//                //文件夹不设置大小
//                //file对象的lastModified是一个长整型，以时间戳为单位
////                setCommonFile(file.getName(),file.getPath(),true,file.lastModified(),meta);
//                //将当前文件夹保存到list集合中
////                fileMetas.add(meta);
//                //++i
//                dirNum.decrementAndGet();
//                scan(file);
//            } else {
//                //是个文件
////                setCommonFile(file.getName(),file.getPath(),false,file.lastModified(),meta);
////                //文件有大小，file.length()默认以字节为单位 长整型
////                meta.setSize(file.length());
////                //保存文件
////                fileMetas.add(meta);
//                //++i
//                fileNum.incrementAndGet();
//            }
//        }
    }

    //具体扫描任务的子线程递归方法
    private void scanInternal(File filePath) {
        if (filePath == null) {
            return;
        }
        //将当前要扫描的任务交给线程处理
        //这个任务就是在扫描文件和文件夹
        pool.submit( () -> {
            //使用回调函数 将当前目录下的所有内容保存到指定终端
            this.callBack.callback(filePath);
            //先将这一级目录下的file对象获取出来
            File[] files = filePath.listFiles();
            //遍历这些file对象，根据是否是文件夹进行区别处理
            for (File file : files) {
                if (file.isDirectory()) {
                    //文件夹个数＋1
                    dirNum.incrementAndGet();
                    //线程数量＋ 1 碰到文件夹递归创建新线程
                    threadCount.incrementAndGet();
                    //将子文件夹的任务交给新线程处理 新线程执行子文件夹的扫描和保存工作
                    scanInternal(file);
                } else {
                    fileNum.incrementAndGet();
                    
                }
            }
            //for循环走完，说明当前线程的保存和扫描任务结束，当前线程就将ThreadCount这个值-1；
            System.out.println(Thread.currentThread().getName()+"扫描"+filePath +"任务结束");
            //子线程数-1
            threadCount.decrementAndGet();
            //判断是不是最后一个线程
            if (threadCount.get() == 0) {
                //所有线程已经结束任务
                System.out.println("所有扫描任务结束");
                //唤醒主线程
                latch.countDown();
            }
        });
    }
    //设置文件共有属性
//    private void setCommonFile(String name,String path,boolean isDir,Long lastModified,FileMeta meta) {
//        meta.setName(name);
//        meta.setPath(path);
//        //文件夹不设置大小
//        meta.setIsDirectory(isDir);
//        //file对象的lastModified是一个长整型，以时间戳为单位
//        meta.setLastModified(new Date(lastModified));
//    }
}
