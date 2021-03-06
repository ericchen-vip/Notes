package algorithms.sort;

import algorithms.sort.base.RunnableSort;

/**
 * <p>
 * TODO
 * </p>
 *
 * @author EricChen 2021/02/18 16:20
 */
public interface MergeSort extends RunnableSort {


    @Override
    default void doSort(int[] array, int length) {
        doMergeSort(array, 0, length -1);
    }


    void doMergeSort(int[] array, int left, int right);


}
