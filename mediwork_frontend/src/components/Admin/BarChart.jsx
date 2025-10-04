import React, { useRef, useEffect } from 'react';
import Chart from 'chart.js/auto';

const BarChart = ({ data, title, legends = true }) => {
    const chartRef = useRef(null);
    const chartInstance = useRef(null);

    const defaultData = [
        { x: 'Jan', applied: 19, left: 16 },
        { x: 'Feb', applied: 15, left: 8 },
        { x: 'Mar', applied: 24, left: 14 },
        { x: 'Apr', applied: 8, left: 7 },
        { x: 'May', applied: 11, left: 7 },
        { x: 'Jun', applied: 10, left: 6 },
        { x: 'Jul', applied: 17, left: 15 },
        { x: 'Aug', applied: 19, left: 18 },
        { x: 'Sep', applied: 18, left: 12 },
        { x: 'Oct', applied: 19, left: 20 },
        { x: 'Nov', applied: 18, left: 8 },
        { x: 'Dec', applied: 17, left: 12 },
    ];

    const chartData = {
        labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
        datasets: [{
            label: 'Applied',
            borderRadius: 5,
            data: data || defaultData,
            backgroundColor: '#FF7A00',
            maxBarThickness: 20,
            parsing: {
                yAxisKey: 'applied'
            },
        },
        {
            label: 'Left',
            borderRadius: 5,
            maxBarThickness: 20,
            data: data || defaultData,
            backgroundColor: '#6F52ED',
            parsing: {
                yAxisKey: 'left'
            },
        }]
    };

    useEffect(() => {
        if (chartRef.current) {
            // Destroy existing chart if it exists
            if (chartInstance.current) {
                chartInstance.current.destroy();
            }

            const ctx = chartRef.current.getContext('2d');
            chartInstance.current = new Chart(ctx, {
                type: 'bar',
                data: chartData,
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: true,
                            max: 25,
                            grid: {
                                display: true
                            }
                        },
                        x: {
                            grid: {
                                display: false
                            }
                        },
                    },
                    plugins: {
                        legend: {
                            display: false
                        }
                    }
                }
            });
        }

        // Cleanup function
        return () => {
            if (chartInstance.current) {
                chartInstance.current.destroy();
            }
        };
    }, [data]);

    return (
        <div className="flex flex-col justify-center px-8 py-6 bg-white rounded-lg shadow-md shadow-gray-200 md:col-span-2 md:row-span-2 gap-y-4 gap-x-8">
            <div className="sm:flex sm:items-center sm:justify-between">
                <h2 className="font-medium text-gray-700">{title || "Monthly Statistics"}</h2>

                {legends && (
                    <div className="flex items-center mt-4 -mx-2 sm:mt-0">
                        <span className="flex items-center text-gray-600">
                            <p className="mx-2">Applied</p>
                            <span className="w-2 h-2 mx-2 bg-orange-500 rounded-full"></span>
                        </span>

                        <span className="flex items-center text-gray-600">
                            <p className="mx-2">Left</p>
                            <span className="w-2 h-2 mx-2 bg-indigo-500 rounded-full"></span>
                        </span>
                    </div>
                )}
            </div>

            <div className="relative h-80">
                <canvas ref={chartRef} className="w-full h-full"></canvas>
            </div>
        </div>
    );
};

export default BarChart;
