const { createApp, ref, computed, onMounted, nextTick } = Vue;

createApp({
    setup() {
        const operadoras = ref([]);
        const estatisticas = ref({ total_despesas: 0, distribuicao_uf: [] });
        const rankingOperadoras = ref([]); 
        const meta = ref({ page: 1, pages: 1 });

        const searchQuery = ref("");
        const selectedUF = ref("");
        const chartSelectedUF = ref("");
        const sortBy = ref("nome");
        const limit = ref(10);

        const loading = ref(false);
        const loadingInitial = ref(true); 
        const loadingModal = ref(false);
        const selectedOp = ref(null);
        
        const despesasOp = ref([]);
        const sortDespesasKey = ref('data_referencia'); 
        const sortDespesasOrder = ref('desc'); 

        let chartInstance = null;
        let searchTimer = null;

        const API_URL = "https://ans-api-nicolas.onrender.com/api";

        const formatMoney = (value) => {
            if (value === undefined || value === null) return "0,00";
            const num = Number(value) || 0;

            if (num >= 1e12) {
                return (num / 1e12).toLocaleString('pt-BR', {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2
                }) + " Tri";
            }

            if (num >= 1e9) {
                return (num / 1e9).toLocaleString('pt-BR', {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2
                }) + " Bi";
            }

            return num.toLocaleString('pt-BR', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            });
        };

        const formatDate = (dateString) => {
            if (!dateString) return "-";
            const d = new Date(dateString);
            if (Number.isNaN(d.getTime())) return dateString;
            return d.toLocaleDateString('pt-BR');
        };

        const safeFetch = async (url, options = {}) => {
            const res = await fetch(url, options);
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            return res;
        };

        const getCurrentUF = () => {
            return chartSelectedUF.value || selectedUF.value || "";
        };

        const scrollToOperadoras = async () => {
            await nextTick();
            const section = document.getElementById("operadoras-section");
            if (section) {
                section.scrollIntoView({ behavior: "smooth", block: "start" });
            }
        };

        const fetchEstatisticas = async () => {
            try {
                const res = await safeFetch(`${API_URL}/estatisticas`);
                const data = await res.json();
                estatisticas.value = data || { total_despesas: 0, distribuicao_uf: [] };
                renderChart(estatisticas.value.distribuicao_uf || []);
            } catch (e) {
                console.error("Erro ao buscar estatísticas:", e);
            }
        };

        const fetchRanking = async () => {
            try {
                const res = await safeFetch(`${API_URL}/ranking-operadoras`);
                const data = await res.json();
                rankingOperadoras.value = data || [];
            } catch (e) {
                console.error("Erro ao buscar ranking:", e);
            }
        };

        const fetchOperadoras = async (page = 1, scroll = false) => {
            loading.value = true;
            try {
                let url = `${API_URL}/operadoras?page=${page}&limit=${limit.value}`;

                if (searchQuery.value?.trim()) url += `&search=${encodeURIComponent(searchQuery.value.trim())}`;
                
                const currentUF = getCurrentUF();
                if (currentUF) url += `&uf=${encodeURIComponent(currentUF)}`;
                if (sortBy.value) url += `&sort=${encodeURIComponent(sortBy.value)}`;

                const res = await safeFetch(url);
                const json = await res.json();

                operadoras.value = json.data || [];
                meta.value = json.meta || { page: 1, pages: 1 };

                if (scroll) await scrollToOperadoras();
            } catch (e) {
                console.error("Erro ao buscar operadoras:", e);
                operadoras.value = [];
            } finally {
                loading.value = false;
            }
        };

        const debouncedFetch = () => {
            clearTimeout(searchTimer);
            searchTimer = setTimeout(() => {
                meta.value.page = 1;
                fetchOperadoras(1);
            }, 500);
        };

        const applyFilters = async () => { chartSelectedUF.value = ""; meta.value.page = 1; await fetchOperadoras(1, true); };
        const filterByUF = async (uf) => { chartSelectedUF.value = ""; selectedUF.value = uf; meta.value.page = 1; await fetchOperadoras(1, true); };
        const clearChartFilter = async () => { chartSelectedUF.value = ""; meta.value.page = 1; renderChart(estatisticas.value.distribuicao_uf || []); await fetchOperadoras(1); };
        const clearFilters = async () => { searchQuery.value = ""; selectedUF.value = ""; chartSelectedUF.value = ""; sortBy.value = "nome"; meta.value.page = 1; renderChart(estatisticas.value.distribuicao_uf || []); await fetchOperadoras(1); };

        const openModal = async (op) => {
            selectedOp.value = op;
            despesasOp.value = [];
            
            sortDespesasKey.value = 'data_referencia';
            sortDespesasOrder.value = 'desc';
            
            loadingModal.value = true;

            const modal = new bootstrap.Modal(document.getElementById("detailsModal"));
            modal.show();

            try {
                const res = await safeFetch(`${API_URL}/operadoras/${op.registro_ans}/despesas`);
                const json = await res.json();
                despesasOp.value = Array.isArray(json) ? json : [];
            } catch (e) {
                console.error("Erro ao buscar despesas:", e);
                despesasOp.value = [];
            } finally {
                loadingModal.value = false;
            }
        };

        const toggleSortDespesas = (key) => {
            if (sortDespesasKey.value === key) {
                sortDespesasOrder.value = sortDespesasOrder.value === 'asc' ? 'desc' : 'asc';
            } else {
                sortDespesasKey.value = key;
                sortDespesasOrder.value = 'desc'; 
            }
        };

        const sortedDespesas = computed(() => {
            let list = [...despesasOp.value];
            
            list.sort((a, b) => {
                let valA = a[sortDespesasKey.value];
                let valB = b[sortDespesasKey.value];

                if (sortDespesasKey.value === 'data_referencia') {
                    valA = new Date(valA).getTime();
                    valB = new Date(valB).getTime();
                } else if (sortDespesasKey.value === 'valor') {
                    valA = Number(valA) || 0;
                    valB = Number(valB) || 0;
                }

                if (valA < valB) return sortDespesasOrder.value === 'asc' ? -1 : 1;
                if (valA > valB) return sortDespesasOrder.value === 'asc' ? 1 : -1;
                return 0;
            });
            
            return list;
        });

        const availableUFs = ["AC", "AL", "AM", "AP", "BA", "CE", "DF", "ES", "GO", "MA", "MG", "MS", "MT", "PA", "PB", "PE", "PI", "PR", "RJ", "RN", "RO", "RR", "RS", "SC", "SE", "SP", "TO"];

        const filteredOperadoras = computed(() => {
            let list = [...operadoras.value];
            if (sortBy.value === "nome") {
                list.sort((a, b) => (a.razao_social || "").localeCompare((b.razao_social || ""), "pt-BR"));
            } else if (sortBy.value === "uf") {
                list.sort((a, b) => (a.uf || "").localeCompare((b.uf || ""), "pt-BR"));
            } else if (sortBy.value === "registro") {
                list.sort((a, b) => String(a.registro_ans || "").localeCompare(String(b.registro_ans || ""), "pt-BR"));
            }
            return list;
        });

        const currentSelectedLabel = computed(() => {
            if (chartSelectedUF.value) return `Estado selecionado no gráfico: ${chartSelectedUF.value}`;
            if (selectedUF.value) return `Estado selecionado: ${selectedUF.value}`;
            return "Sem filtro por estado";
        });

        const renderChart = (data = []) => {
            const canvas = document.getElementById("chartUF");
            if (!canvas) return;
            if (chartInstance) chartInstance.destroy();

            const chartData = data;

            chartInstance = new Chart(canvas, {
                type: "bar",
                data: {
                    labels: chartData.map(item => item.uf),
                    datasets: [{
                        label: "Despesas por UF",
                        data: chartData.map(item => Number(item.total) || 0),
                        backgroundColor: chartData.map(item => chartSelectedUF.value === item.uf ? "rgba(31, 111, 255, 1)" : "rgba(31, 111, 255, 0.72)"),
                        hoverBackgroundColor: chartData.map(item => chartSelectedUF.value === item.uf ? "rgba(31, 111, 255, 1)" : "rgba(31, 111, 255, 0.88)"),
                        borderRadius: 14, borderSkipped: false, maxBarThickness: 58, minBarLength: 8
                    }]
                },
                options: {
                    responsive: true, maintainAspectRatio: false, animation: { duration: 700 },
                    onClick: async (event, elements) => {
                        if (!elements.length) return;
                        const index = elements[0].index;
                        const clickedUF = chartData[index]?.uf;
                        if (!clickedUF) return;

                        if (chartSelectedUF.value === clickedUF) chartSelectedUF.value = "";
                        else { chartSelectedUF.value = clickedUF; selectedUF.value = ""; }

                        meta.value.page = 1;
                        renderChart(estatisticas.value.distribuicao_uf || []);
                        await fetchOperadoras(1, true);
                    },
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            backgroundColor: "#132238", padding: 12, cornerRadius: 12,
                            callbacks: { label: function(context) { return " R$ " + Number(context.raw || 0).toLocaleString("pt-BR", { minimumFractionDigits: 2, maximumFractionDigits: 2 }); } }
                        }
                    },
                    scales: {
                        y: { beginAtZero: true, grid: { color: "#edf2f7", drawBorder: false }, ticks: { color: "#7b8794", callback: function(value) { return "R$ " + Number(value).toLocaleString("pt-BR"); } } },
                        x: { grid: { display: false, drawBorder: false }, ticks: { color: "#5f6c7b", font: { weight: "600" }, autoSkip: false, maxRotation: 45, minRotation: 45 } }
                    }
                }
            });
        };

        onMounted(async () => {
            loadingInitial.value = true;
            try {
                await Promise.all([fetchEstatisticas(), fetchRanking(), fetchOperadoras(1)]);
            } finally { loadingInitial.value = false; }
        });

        return {
            operadoras, estatisticas, rankingOperadoras, meta, searchQuery, selectedUF, chartSelectedUF, sortBy, limit,
            loading, loadingInitial, loadingModal, selectedOp, availableUFs, filteredOperadoras, currentSelectedLabel,
            sortedDespesas, sortDespesasKey, sortDespesasOrder, 
            formatMoney, formatDate, fetchOperadoras, debouncedFetch, openModal, filterByUF, clearFilters, clearChartFilter, applyFilters, toggleSortDespesas
        };
    }
}).mount("#app");