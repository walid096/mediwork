import React, { useRef, useEffect } from 'react';
import Chart from 'chart.js/auto';

const DoughnutChart = ({ data, title }) => {
    const chartRef = useRef(null);
    const chartInstance = useRef(null);

    const defaultData = {
        labels: ['Active Users', 'Inactive Users', 'Pending', 'Suspended', 'Other'],
        datasets: [{
            label: 'User Distribution',
            data: [45, 25, 15, 10, 5],
            backgroundColor: [
                'rgba(153, 102, 255, 0.8)',
                'rgba(255, 206, 86, 0.8)',
                'rgba(255, 99, 132, 0.8)',
                'rgba(54, 162, 235, 0.8)',
                'rgba(75, 192, 192, 0.8)',
            ],
            borderColor: [
                'rgba(153, 102, 255, 1)',
                'rgba(255, 206, 86, 1)',
                'rgba(255, 99, 132, 1)',
                'rgba(54, 162, 235, 1)',
                'rgba(75, 192, 192, 1)',
            ],
            borderWidth: 2
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
                type: 'doughnut',
                data: data || defaultData,
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom',
                            fullSize: false,
                            align: 'start',
                            labels: {
                                padding: 10,
                                usePointStyle: true,
                                pointStyle: 'circle'
                            }
                        }
                    },
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
        <div className="bg-white rounded-lg shadow-md shadow-gray-200 lg:row-span-2">
            <div className="px-6 py-5 border-b border-gray-100">
                <h2 className="font-medium text-gray-700">{title || "Distribution Chart"}</h2>
            </div>
            <div className="flex items-center justify-center p-8">
                <div className="relative w-full h-64">
                    <canvas ref={chartRef} className="max-w-sm mx-auto"></canvas>
                </div>
            </div>
        </div>
    );
};

export default DoughnutChart;
